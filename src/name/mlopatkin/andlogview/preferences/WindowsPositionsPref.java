/*
 * Copyright 2022 the Andlogview authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package name.mlopatkin.andlogview.preferences;

import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.ConfigStorageClient;
import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.config.InvalidJsonContentException;
import name.mlopatkin.andlogview.config.NamedClient;
import name.mlopatkin.andlogview.config.Preference;
import name.mlopatkin.andlogview.ui.FrameDimensions;
import name.mlopatkin.andlogview.ui.FrameLocation;

import com.google.errorprone.annotations.Immutable;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WindowsPositionsPref {
    public enum Frame {
        MAIN("main", getLegacyMainFrameInfo());

        private final String prefName;
        private final FrameInfo defaultInfo;

        Frame(String prefName, FrameInfo defaultInfo) {
            this.prefName = prefName;
            this.defaultInfo = defaultInfo;
        }

        public String getPrefName() {
            return prefName;
        }

        FrameInfo getDefaultInfo() {
            return defaultInfo;
        }
    }

    private static final Logger logger = Logger.getLogger(WindowsPositionsPref.class);

    private static final ConfigStorageClient<Map<Frame, FrameInfo>> STORAGE_CLIENT =
            new NamedClient<>("windows") {
                @Override
                public Map<Frame, FrameInfo> fromJson(Gson gson, JsonElement element)
                        throws InvalidJsonContentException {
                    if (!element.isJsonObject()) {
                        throw new InvalidJsonContentException("Expecting JSON object here");
                    }
                    JsonObject json = element.getAsJsonObject();
                    EnumMap<Frame, FrameInfo> result = new EnumMap<>(Frame.class);
                    // Get as much info as we can, ignoring invalid frame data.
                    for (Frame frame : Frame.values()) {
                        if (json.has(frame.getPrefName())) {
                            JsonElement frameData = json.get(frame.getPrefName());
                            try {
                                FrameInfoSerialized frameInfoSerialized =
                                        gson.fromJson(frameData, FrameInfoSerialized.class);
                                result.put(frame, frameInfoSerialized.tryDeserialize());
                            } catch (JsonSyntaxException | InvalidJsonContentException e) {
                                logger.error("Incorrect entry for frame " + frame, e);
                            }
                        }
                    }
                    return result;
                }

                @Override
                public Map<Frame, FrameInfo> getDefault() {
                    return Collections.emptyMap();
                }

                @Override
                public JsonElement toJson(Gson gson, Map<Frame, FrameInfo> value) {
                    JsonObject json = new JsonObject();
                    value.forEach(
                            (frame, info) -> json.add(frame.getPrefName(),
                                    gson.toJsonTree(info.toSerialized(), FrameInfoSerialized.class)));
                    return json;
                }
            };


    private final Preference<Map<Frame, FrameInfo>> frameInfoPref;

    @Inject
    public WindowsPositionsPref(ConfigStorage configStorage) {
        this.frameInfoPref = configStorage.preference(STORAGE_CLIENT).memoize();
    }

    public Optional<FrameLocation> getFrameLocation(Frame frame) {
        return getFrameInfo(frame).getLocation();
    }

    public FrameDimensions getFrameDimensions(Frame frame) {
        return getFrameInfo(frame).getDimensions();
    }

    public void setFrameInfo(Frame frame, FrameLocation location, FrameDimensions dimensions) {
        EnumMap<Frame, FrameInfo> updatedMap = new EnumMap<>(Frame.class);
        updatedMap.putAll(frameInfoPref.get());
        updatedMap.put(frame, new FrameInfo(location, dimensions));
        frameInfoPref.set(updatedMap);
    }

    private FrameInfo getFrameInfo(Frame frame) {
        return frameInfoPref.get().getOrDefault(frame, frame.getDefaultInfo());
    }

    private static int getSizeWithFallback(int size, int fallbackSize) {
        if (size <= 0) {
            return fallbackSize;
        }
        return size;
    }

    @SuppressWarnings("deprecation")
    private static FrameInfo getLegacyMainFrameInfo() {
        FrameLocation location = null;
        if (Configuration.ui.mainWindowPosition() != null) {
            location =
                    new FrameLocation(Configuration.ui.mainWindowPosition().x, Configuration.ui.mainWindowPosition().y);
        }
        FrameDimensions dimensions = new FrameDimensions(
                getSizeWithFallback(Configuration.ui.mainWindowWidth(), 1024),
                getSizeWithFallback(Configuration.ui.mainWindowHeight(), 768));
        return new FrameInfo(location, dimensions);
    }

    @Immutable
    private static class FrameInfo {
        private final @Nullable FrameLocation location;
        private final FrameDimensions dimensions;

        public FrameInfo(@Nullable FrameLocation location, FrameDimensions dimensions) {
            this.location = location;
            this.dimensions = dimensions;
        }

        public Optional<FrameLocation> getLocation() {
            return Optional.ofNullable(location);
        }

        public FrameDimensions getDimensions() {
            return dimensions;
        }

        public FrameInfoSerialized toSerialized() {
            if (location == null) {
                return new FrameInfoSerialized(dimensions.width, dimensions.height);
            }
            return new FrameInfoSerialized(location.x, location.y, dimensions.width, dimensions.height);
        }
    }

    // Serialized form of the frame info has no built-in checks
    private static class FrameInfoSerialized {
        private final @Nullable Integer x;
        private final @Nullable Integer y;
        private final int width;
        private final int height;

        FrameInfoSerialized(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        FrameInfoSerialized(int width, int height) {
            this.x = null;
            this.y = null;
            this.width = width;
            this.height = height;
        }

        public FrameInfo tryDeserialize() throws InvalidJsonContentException {
            @Nullable FrameLocation location = (x != null && y != null) ? new FrameLocation(x, y) : null;
            if (width <= 0 || height <= 0) {
                throw new InvalidJsonContentException("Unsupported window size (%d x %d)", width, height);
            }
            FrameDimensions dimensions = new FrameDimensions(width, height);
            return new FrameInfo(location, dimensions);
        }
    }
}

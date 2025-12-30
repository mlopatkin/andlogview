/*
 * Copyright 2014 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.config;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.errorprone.annotations.ThreadSafe;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ThreadSafe
class ConfigStorageImpl implements ConfigStorage {
    private static final Logger logger = LoggerFactory.getLogger(ConfigStorageImpl.class);

    private final Gson gson;
    private final CharSource inStorage;
    private final CharSink outStorage;
    private final ExecutorService fileWorker;

    @GuardedBy("serializedConfig")
    private final Map<String, JsonElement> serializedConfig = Maps.newHashMap();
    @GuardedBy("serializedConfig")
    private boolean dirty = false;

    private final Runnable fileSaver = this::save;

    public ConfigStorageImpl(Gson gson, CharSource inStorage, CharSink outStorage, ExecutorService fileWorker) {
        this.gson = gson;
        this.inStorage = inStorage;
        this.outStorage = outStorage;
        this.fileWorker = fileWorker;
    }

    @VisibleForTesting
    ConfigStorageImpl(CharSource inStorage, CharSink outStorage, ExecutorService fileWorker) {
        this(Utils.createConfigurationGson(), inStorage, outStorage, fileWorker);
    }

    void load() {
        try (Reader in = inStorage.openBufferedStream()) {
            load(JsonParser.parseReader(in));
            logger.debug("Successfully parsed config data");
        } catch (IOException e) {
            logger.error("Failed to open JSON config data file", e);
        } catch (JsonParseException e) {
            logger.error("Failed to parse JSON data", e);
        }
    }

    void load(JsonElement element) {
        if (element.isJsonNull()) {
            // a case of empty stream
            return;
        }
        if (!element.isJsonObject()) {
            logger.error("Root element is not an object");
            return;
        }

        JsonObject obj = element.getAsJsonObject();
        synchronized (serializedConfig) {
            serializedConfig.clear();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                serializedConfig.put(entry.getKey(), entry.getValue());
            }
            scheduleCommitLocked();
        }
    }

    private void save() {
        Map<String, JsonElement> elements;
        synchronized (serializedConfig) {
            elements = Maps.newHashMap(serializedConfig);
            dirty = false;
        }
        saveImpl(elements);
    }

    private void saveImpl(Map<String, JsonElement> filters) {
        try (Writer out = outStorage.openBufferedStream(); JsonWriter writer = new JsonWriter(out)) {
            writer.setIndent("  "); // some pretty-print
            saveToJsonWriter(writer, filters);
            logger.debug("Successfully written config data, commiting");
        } catch (IOException e) {
            logger.error("Failed to open storage for writing", e);
        }
    }

    private void saveToJsonWriter(JsonWriter writer, Map<String, JsonElement> filters) throws IOException {
        try {
            writer.beginObject();
            for (Map.Entry<String, JsonElement> entry : filters.entrySet()) {
                writer.name(entry.getKey());
                gson.toJson(entry.getValue(), writer);
            }
            writer.endObject();
        } catch (JsonIOException e) {
            logger.error("Failed to save JSON data", e);
        }
    }

    @GuardedBy("serializedConfig")
    @SuppressWarnings("FutureReturnValueIgnored")
    private void scheduleCommitLocked() {
        if (!dirty) {
            dirty = true;
            // There is no need to investigate the return value of the future, UncaughtExceptionHandler is okay
            fileWorker.submit(fileSaver);
        }
    }

    // in both saveConfig and loadConfig we avoid to call alien methods with the lock held
    @Override
    public <T> void saveConfig(ConfigStorageClient<T> client, T value) {
        String name = client.getName();
        JsonElement json = client.toJson(gson, value);
        logger.debug("Client {} changed the config", name);
        synchronized (serializedConfig) {
            serializedConfig.put(name, json);
            scheduleCommitLocked();
        }
    }

    @Override
    public <T> T loadConfig(ConfigStorageClient<T> client) {
        String clientName = client.getName();
        JsonElement element;
        synchronized (serializedConfig) {
            element = serializedConfig.get(clientName);
        }
        try {
            if (element != null) {
                return client.fromJson(gson, element);
            }
        } catch (JsonSyntaxException | InvalidJsonContentException e) {
            // We have some weird JSON for this client. Discard it unless somebody updated it in
            // background.
            synchronized (serializedConfig) {
                if (serializedConfig.get(clientName) == element) {
                    serializedConfig.remove(clientName);
                    scheduleCommitLocked();
                }
            }
            logger.error("Failed to parse config data of {}", client.getName(), e);
        }
        // failed to load/parse, provide fallback
        return client.getDefault();
    }

    @Override
    public boolean hasStoredDataFor(String clientName) {
        synchronized (serializedConfig) {
            return serializedConfig.containsKey(clientName);
        }
    }

    void shutdown(boolean waitForCompletion) throws InterruptedException {
        Future<?> saveResult = fileWorker.submit(fileSaver);
        if (waitForCompletion) {
            try {
                saveResult.get(120, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                logger.warn("Failed to terminate file worker properly", e);
            } catch (TimeoutException e) {
                logger.warn("Failed to terminate file worker in time");
            }
        }
    }
}

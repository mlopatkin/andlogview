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

package org.bitbucket.mlopatkin.android.logviewer.config;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.logviewer.AtExitManager;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Provides an access to a persistent storage for filters.
 */
@ThreadSafe
public class ConfigStorage {
    private static final Logger logger = Logger.getLogger(ConfigStorage.class);

    public static class InvalidJsonContentException extends Exception {

        public InvalidJsonContentException(String message) {
            super(message);
        }

        public InvalidJsonContentException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public interface ConfigStorageClient<T> {

        String getName();

        T fromJson(Gson gson, JsonElement element) throws InvalidJsonContentException;

        T getDefault();

        JsonElement toJson(Gson gson, T value);
    }

    private final CharSource inStorage;
    private final CharSink outStorage;
    private final ExecutorService fileWorker;
    private final Gson gson = new Gson();

    @GuardedBy("serializedConfig")
    private final Map<String, JsonElement> serializedConfig = Maps.newHashMap();
    @GuardedBy("serializedConfig")
    private boolean dirty = false;

    private final Runnable fileSaver = this::save;

    @VisibleForTesting
    public ConfigStorage(CharSource inStorage, CharSink outStorage, ExecutorService fileWorker) {
        this.inStorage = inStorage;
        this.outStorage = outStorage;
        this.fileWorker = fileWorker;
    }

    void load() {
        JsonParser parser = new JsonParser();
        try (Reader in = inStorage.openBufferedStream()) {
            load(parser.parse(in));
            logger.debug("Successfully parsed config data");
        } catch (IOException e) {
            logger.error("Failed to open JSON config data file", e);
        } catch (JsonParseException e) {
            logger.error("Failed to parse JSON data", e);
        }
    }

    private void load(JsonElement element) {
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

    public void save() {
        Map<String, JsonElement> elements;
        synchronized (serializedConfig) {
            elements = Maps.newHashMap(serializedConfig);
            dirty = false;
        }
        saveImpl(elements);
    }

    private void saveImpl(Map<String, JsonElement> filters) {
        try (Writer out = outStorage.openBufferedStream();
             JsonWriter writer = new JsonWriter(out)) {
            writer.setIndent("  "); // some pretty-print
            saveToJsonWriter(writer, filters);
            logger.debug("Successfully written config data, commiting");
        } catch (IOException e) {
            logger.error("Failed to open storage for writing", e);
        }
    }

    private void saveToJsonWriter(JsonWriter writer, Map<String, JsonElement> filters)
            throws IOException {
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
    private void scheduleCommitLocked() {
        if (!dirty) {
            dirty = true;
            fileWorker.submit(fileSaver);
        }
    }

    // in both saveConfig and loadConfig we avoid to call alien methods with the lock held
    public <T> void saveConfig(ConfigStorageClient<T> client, T value) {
        String name = client.getName();
        JsonElement json = client.toJson(gson, value);
        synchronized (serializedConfig) {
            serializedConfig.put(name, json);
            scheduleCommitLocked();
        }
    }

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
            logger.error("Failed to parse config data of " + client.getName(), e);
        }
        // failed to load/parse, provide fallback
        return client.getDefault();
    }

    public void shutdown(boolean waitForCompletion) throws InterruptedException {
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

    @Singleton
    public static class Factory {
        private final ExecutorService ioThreadPool;
        private final AtExitManager atExitManager;

        @Inject
        Factory(@Named(ConfigModule.CONFIG_THREAD_POOL) ExecutorService ioThreadPool, AtExitManager atExitManager) {
            this.ioThreadPool = ioThreadPool;
            this.atExitManager = atExitManager;
        }

        public ConfigStorage createForFile(File file) throws IOException {
            if (!file.exists()) {
                Files.touch(file);
            }
            final ConfigStorage result = new ConfigStorage(
                    Files.asCharSource(file, Charsets.UTF_8),
                    Files.asCharSink(file, Charsets.UTF_8),
                    ioThreadPool);
            atExitManager.registerExitAction(() -> {
                try {
                    result.shutdown(true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            result.load();
            return result;
        }
    }
}

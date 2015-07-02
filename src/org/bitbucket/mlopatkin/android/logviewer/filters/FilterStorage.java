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

package org.bitbucket.mlopatkin.android.logviewer.filters;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.utils.Threads;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Provides an access to a persistent storage for filters.
 */
@ThreadSafe
public class FilterStorage {
    // TODO this has little to do with filters per se, rename to storage
    private static final Logger logger = Logger.getLogger(FilterStorage.class);

    public static class InvalidJsonContentException extends Exception {

        public InvalidJsonContentException(String message) {
            super(message);
        }

        public InvalidJsonContentException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public interface FilterStorageClient<T> {

        String getName();

        T fromJson(Gson gson, JsonElement element) throws InvalidJsonContentException;

        T getDefault();

        JsonElement toJson(Gson gson, T value);
    }

    private final CharSource inStorage;
    private final CharSink outStorage;
    private final ExecutorService fileWorker;
    private final Gson gson = new Gson();

    @GuardedBy("serializedFilters")
    private final Map<String, JsonElement> serializedFilters = Maps.newHashMap();
    @GuardedBy("serializedFilters")
    private boolean dirty = false;

    private final Runnable fileSaver = new Runnable() {
        @Override
        public void run() {
            save();
        }
    };

    FilterStorage(CharSource inStorage, CharSink outStorage, ExecutorService fileWorker) {
        this.inStorage = inStorage;
        this.outStorage = outStorage;
        this.fileWorker = fileWorker;
//        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public static FilterStorage createForFile(File file) throws IOException {
        if (!file.exists()) {
            Files.touch(file);
        }
        final FilterStorage result = new FilterStorage(
                Files.asCharSource(file, Charsets.UTF_8),
                Files.asCharSink(file, Charsets.UTF_8),
                Executors.newSingleThreadExecutor(
                        new ThreadFactoryBuilder().setThreadFactory(Threads.withName("FilterStorageFileWorker"))
                                                  .setDaemon(true).build()));
        Runtime.getRuntime().addShutdownHook(new Thread("OnExitCommiter") {
            @Override
            public void run() {
                try {
                    result.shutdown(true);
                } catch (InterruptedException e) {
                    interrupt();
                }
            }
        });
        result.load();
        return result;
    }

    public void load() {
        JsonParser parser = new JsonParser();
        try (Reader in = inStorage.openBufferedStream()) {
            load(parser.parse(in));
            logger.debug("Successfully parsed filter data");
        } catch (IOException e) {
            logger.error("Failed to open JSON filter data file", e);
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
        synchronized (serializedFilters) {
            serializedFilters.clear();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                serializedFilters.put(entry.getKey(), entry.getValue());
            }
            scheduleCommitLocked();
        }
    }

    public void save() {
        Map<String, JsonElement> elements;
        synchronized (serializedFilters) {
            elements = Maps.newHashMap(serializedFilters);
            dirty = false;
        }
        saveImpl(elements);
    }

    private void saveImpl(Map<String, JsonElement> filters) {
        try (Writer out = outStorage.openBufferedStream();
             JsonWriter writer = new JsonWriter(out)) {
            saveToJsonWriter(writer, filters);
            logger.debug("Successfully written filter data, commiting");
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

    @GuardedBy("serializedFilters")
    private void scheduleCommitLocked() {
        if (!dirty) {
            dirty = true;
            fileWorker.submit(fileSaver);
        }
    }

    // in both saveFilters and loadFilters we avoid to call alien methods with the lock held
    public <T> void saveFilters(FilterStorageClient<T> client, T value) {
        String name = client.getName();
        JsonElement json = client.toJson(gson, value);
        synchronized (serializedFilters) {
            serializedFilters.put(name, json);
            scheduleCommitLocked();
        }
    }

    public <T> T loadFilters(FilterStorageClient<T> client) {
        String clientName = client.getName();
        JsonElement element;
        synchronized (serializedFilters) {
            element = serializedFilters.get(clientName);
        }
        try {
            if (element != null) {
                return client.fromJson(gson, element);
            }
        } catch (JsonSyntaxException | InvalidJsonContentException e) {
            // We have some weird JSON for this client. Discard it unless somebody updated it in
            // background.
            synchronized (serializedFilters) {
                if (serializedFilters.get(clientName) == element) {
                    serializedFilters.remove(clientName);
                    scheduleCommitLocked();
                }
            }
            logger.error("Failed to parse filter data of " + client.getName(), e);
        }
        // failed to load/parse, provide fallback
        return client.getDefault();
    }

    public void shutdown(boolean waitForCompletion) throws InterruptedException {
        fileWorker.submit(fileSaver);
        fileWorker.shutdown();
        if (waitForCompletion && !fileWorker.awaitTermination(120, TimeUnit.SECONDS)) {
            logger.warn("Failed to terminate file worker properly");
        }
    }
}

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

import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConfigStorageImplTest {
    public static class TestClientData {
        String s = "";

        public TestClientData() {}

        public TestClientData(String s) {
            this.s = s;
        }
    }

    public static class TestClient implements ConfigStorageImpl.ConfigStorageClient<TestClientData> {
        @Override
        public String getName() {
            return "TestClient";
        }

        @Override
        public TestClientData fromJson(Gson gson, JsonElement element) {
            return gson.fromJson(element, TestClientData.class);
        }

        @Override
        public TestClientData getDefault() {
            return new TestClientData();
        }

        @Override
        public JsonElement toJson(Gson gson, TestClientData value) {
            return gson.toJsonTree(value);
        }
    }

    @Test
    public void testLoad() throws Exception {
        CharSource in = CharSource.wrap("{\"TestClient\":{\"s\":\"123456\"}}");
        CharSink out = new NullCharSink();

        ConfigStorageImpl storage = new ConfigStorageImpl(in, out, MoreExecutors.newDirectExecutorService());
        storage.load();

        assertEquals("123456", storage.loadConfig(new TestClient()).s);
    }

    @Test
    public void testLoad_emptyInput() throws Exception {
        CharSource in = CharSource.empty();
        StringCharSink out = new StringCharSink();

        ConfigStorageImpl storage = new ConfigStorageImpl(in, out, MoreExecutors.newDirectExecutorService());
        storage.load();

        assertEquals("", storage.loadConfig(new TestClient()).s);
        assertNull("Do not commit default entry if there is no entry at all", out.getLastWrittenString());
    }

    @Test
    public void testLoad_malformedInput() throws Exception {
        CharSource in = CharSource.wrap("{\"TestClient\":[]}");
        StringCharSink out = new StringCharSink();

        ConfigStorageImpl storage = new ConfigStorageImpl(in, out, MoreExecutors.newDirectExecutorService());
        storage.load();

        assertEquals("", storage.loadConfig(new TestClient()).s);
        assertEquals("{}", out.getLastWrittenString());
    }

    @Test
    public void testSave() throws Exception {
        CharSource in = CharSource.empty();
        StringCharSink out = new StringCharSink();

        ConfigStorage storage = new ConfigStorageImpl(in, out, MoreExecutors.newDirectExecutorService());

        storage.saveConfig(new TestClient(), new TestClientData("123456"));

        assertEquals("{\n"
                        + "  \"TestClient\": {\n"
                        + "    \"s\": \"123456\"\n"
                        + "  }\n"
                        + "}",
                out.getLastWrittenString());
    }

    @Test
    public void testSave_writeFailsButWorks() throws Exception {
        CharSource in = CharSource.empty();
        CharSink out = new FailCharSink();

        ConfigStorage storage = new ConfigStorageImpl(in, out, MoreExecutors.newDirectExecutorService());

        storage.saveConfig(new TestClient(), new TestClientData("123456"));
    }

    private static class NullCharSink extends CharSink {
        @Override
        public Writer openStream() throws IOException {
            return CharStreams.nullWriter();
        }
    }

    private static class StringCharSink extends CharSink {
        private StringWriter writer;

        @Override
        public Writer openStream() throws IOException {
            writer = new StringWriter();
            return writer;
        }

        public @Nullable String getLastWrittenString() {
            return writer != null ? writer.toString() : null;
        }
    }

    private static class FailCharSink extends CharSink {
        @Override
        public Writer openStream() throws IOException {
            throw new IOException("Expected");
        }
    }
}

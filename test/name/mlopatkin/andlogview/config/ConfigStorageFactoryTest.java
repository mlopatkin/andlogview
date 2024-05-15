/*
 * Copyright 2023 the Andlogview authors
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import name.mlopatkin.andlogview.base.AtExitManager;

import com.google.common.io.MoreFiles;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class ConfigStorageFactoryTest {
    @Test
    void canCreateConfigFileIfDirectoryExists(@TempDir Path tempDir) throws IOException {
        var factory = createFactory();

        var configStorage = factory.createForFile(tempDir.resolve("config.json").toFile());

        assertThat(configStorage).isNotNull();
    }

    @Test
    void canCreateConfigFileIfDirectoryDoesNotExist(@TempDir Path tempDir) throws IOException {
        // https://github.com/mlopatkin/andlogview/issues/317
        var factory = createFactory();

        var configStorage = factory.createForFile(tempDir.resolve("someDir").resolve("config.json").toFile());

        assertThat(configStorage).isNotNull();
    }

    @Test
    void canOpenConfigFileIfItExists(@TempDir Path tempDir) throws IOException {
        var factory = createFactory();

        var existingFile = tempDir.resolve("config.json");
        Files.createFile(existingFile);
        var configStorage = factory.createForFile(existingFile.toFile());

        assertThat(configStorage).isNotNull();
    }

    @Test
    void doesNotOverwriteConfigFileIfItExists(@TempDir Path tempDir) throws IOException {
        var factory = createFactory();

        var existingFile = tempDir.resolve("config.json");
        MoreFiles.asCharSink(existingFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW).write("""
                {
                    "string": "someString"
                }
                """);
        var configStorage = factory.createForFile(existingFile.toFile());

        assertThat(configStorage.preference(new StringClient()).get()).isEqualTo("someString");
    }

    private ConfigStorage.Factory createFactory() {
        return new ConfigStorage.Factory(MoreExecutors.newDirectExecutorService(), mock(AtExitManager.class),
                Utils.createConfigurationGson());
    }

    private static class StringClient implements ConfigStorageClient<String> {
        @Override
        public String getName() {
            return "string";
        }

        @Override
        public String fromJson(Gson gson, JsonElement element) {
            return gson.fromJson(element, String.class);
        }

        @Override
        public String getDefault() {
            return "";
        }

        @Override
        public JsonElement toJson(Gson gson, String value) {
            return gson.toJsonTree(value);
        }
    }
}

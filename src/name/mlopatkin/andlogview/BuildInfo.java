/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview;

import name.mlopatkin.andlogview.base.AppResources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Build metadata about the application.
 */
public class BuildInfo {
    /**
     * Version of the application.
     */
    public static final String VERSION;

    /**
     * Source revision of which app is built.
     */
    public static final String REVISION;

    static {
        var properties = loadProperties();
        VERSION = properties.getProperty("VERSION", "0.0");
        REVISION = properties.getProperty("REVISION", "n/a");
    }

    private static Properties loadProperties() {
        var properties = new Properties();
        try (var in = AppResources.getResource("build-info.properties")
                .asCharSource(StandardCharsets.ISO_8859_1)
                .openBufferedStream()) {
            properties.load(in);
        } catch (IOException ignored) {
            // Reading code will fall back to default values.
        }
        return properties;
    }
}

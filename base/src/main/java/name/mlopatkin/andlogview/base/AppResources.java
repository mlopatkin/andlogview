/*
 * Copyright 2020 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.base;

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;

import java.net.URL;

/**
 * Accessor for application resources.
 */
public final class AppResources {
    private AppResources() {}

    /**
     * Returns a ByteSource for resource in {@code name.mlopatkin.andlogview} package.
     *
     * @param resourcePath relative path to the resource
     * @return the byte source that points to the resource
     * @throws IllegalArgumentException if the resource isn't available
     */
    public static ByteSource getResource(String resourcePath) {
        return Resources.asByteSource(getUrl(resourcePath));
    }

    /**
     * Builds a URL for the resource in {@code name.mlopatkin.andlogview} package.
     * @param resourcePath relative path to the resource
     * @return the URL
     * @throws IllegalArgumentException if the resource isn't available
     */
    public static URL getUrl(String resourcePath) {
        return Resources.getResource(AppResources.class, "/name/mlopatkin/andlogview/" + resourcePath);
    }
}

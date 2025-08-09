/*
 * Copyright 2018 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.ui;

import com.google.common.base.Preconditions;

import java.net.URL;
import java.util.Objects;

/**
 * Built-in icons for UI.
 */
public enum Icons {
    NEXT("icons/legacy/go-next.png", "icons/fontawesome/angle-right.svg"),
    PREVIOUS("icons/legacy/go-previous.png", "icons/fontawesome/angle-left.svg"),
    ADD("icons/legacy/list-add.png", "icons/fontawesome/plus.svg"),
    FILTER("icons/legacy/system-search.png", "icons/fontawesome/magnifying-glass.svg");

    private final String legacyPath;
    private final String modernPath;

    Icons(String legacyPath, String modernPath) {
        this.legacyPath = legacyPath;
        this.modernPath = modernPath;
    }

    public URL getLegacyUrl() {
        return getUrl(legacyPath);
    }

    // TODO(mlopatkin) Use modern URL instead after switching to flatlaf-2.0 which allows to use URL in SVGImage
    //  constructor.
    public String resolveModernPath() {
        return "name/mlopatkin/andlogview/ui/" + modernPath;
    }

    private URL getUrl(String path) {
        URL result = Icons.class.getResource(path);
        Preconditions.checkArgument(result != null, "Can't find resource for path %s", path);
        return Objects.requireNonNull(result);
    }
}

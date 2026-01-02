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

/**
 * Built-in icons for UI.
 */
public enum Icons {
    NEXT("icons/fontawesome/angle-right.svg"),
    PREVIOUS("icons/fontawesome/angle-left.svg"),
    ADD("icons/fontawesome/plus.svg"),
    FILTER("icons/fontawesome/magnifying-glass.svg");

    private final String modernPath;

    Icons(String modernPath) {
        this.modernPath = modernPath;
    }

    // TODO(mlopatkin) Use modern URL instead after switching to flatlaf-2.0 which allows to use URL in SVGImage
    //  constructor.
    public String resolveModernPath() {
        return "name/mlopatkin/andlogview/ui/" + modernPath;
    }

}

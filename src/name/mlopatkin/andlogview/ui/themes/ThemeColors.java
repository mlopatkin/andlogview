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

package name.mlopatkin.andlogview.ui.themes;

import name.mlopatkin.andlogview.logmodel.LogRecord;

import java.awt.Color;
import java.util.List;

/**
 * This class provides access to various colors used in AndLogView.
 */
public interface ThemeColors {
    /**
     * Returns a list of colors to be used as filter highlighting.
     *
     * @return the immutable list of highlight colors
     */
    List<Color> getHighlightColors();

    /**
     * Returns a foreground (text) color to use when displaying log record line with a given priority.
     *
     * @param priority the priority to find color for
     * @return the color
     */
    Color getPriorityForegroundColor(LogRecord.Priority priority);
}

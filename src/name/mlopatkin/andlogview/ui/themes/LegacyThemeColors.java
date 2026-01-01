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
import name.mlopatkin.andlogview.preferences.LegacyConfiguration;

import java.awt.Color;
import java.util.List;

import javax.inject.Inject;

/**
 * This class provides access to colors defined in `logview.properties` files.
 */
public class LegacyThemeColors implements ThemeColors {
    private final LegacyConfiguration legacy;

    @Inject
    public LegacyThemeColors(LegacyConfiguration legacy) {
        this.legacy = legacy;
    }

    @Override
    public List<Color> getHighlightColors() {
        return legacy.ui().highlightColors();
    }

    @Override
    public Color getPriorityForegroundColor(LogRecord.Priority priority) {
        return legacy.ui().priorityColor(priority);
    }

    @Override
    public Color getBackgroundColor() {
        return legacy.ui().backgroundColor();
    }

    @Override
    public Color getBookmarkBackgroundColor() {
        return legacy.ui().bookmarkBackground();
    }

    @Override
    public Color getBookmarkForegroundColor() {
        return legacy.ui().bookmarkedForeground();
    }
}

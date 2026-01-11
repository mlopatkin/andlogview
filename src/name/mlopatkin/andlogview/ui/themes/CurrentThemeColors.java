/*
 * Copyright 2026 the Andlogview authors
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
import java.awt.Font;
import java.util.List;

/**
 * An implementation of {@link ThemeColors} that delegates everything to the current {@link Theme} selected in
 * {@link CurrentTheme}.
 */
public class CurrentThemeColors implements ThemeColors {
    private final CurrentTheme theme;

    public CurrentThemeColors(CurrentTheme theme) {
        this.theme = theme;
    }

    private ThemeColors delegate() {
        return theme.get().getColors();
    }

    @Override
    public List<Color> getHighlightColors() {
        return delegate().getHighlightColors();
    }

    @Override
    public Color getPriorityForegroundColor(LogRecord.Priority priority) {
        return delegate().getPriorityForegroundColor(priority);
    }

    @Override
    public Color getBackgroundColor() {
        return delegate().getBackgroundColor();
    }

    @Override
    public Color getBookmarkBackgroundColor() {
        return delegate().getBookmarkBackgroundColor();
    }

    @Override
    public Color getBookmarkForegroundColor() {
        return delegate().getBookmarkForegroundColor();
    }

    @Override
    public Font configureBookmarkFont(Font baseFont) {
        return delegate().configureBookmarkFont(baseFont);
    }
}

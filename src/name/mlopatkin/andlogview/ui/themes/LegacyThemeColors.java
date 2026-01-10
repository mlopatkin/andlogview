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
 * These are legacy colors used before migrating to JSON themes.
 */
public class LegacyThemeColors implements ThemeColors {
    @Override
    public List<Color> getHighlightColors() {
        return List.of(
                new Color(0xD0F0C0),
                new Color(0xC7C2FC),
                new Color(0xFECBC0),
                new Color(0xFCFC96)
        );
    }

    @Override
    public Color getPriorityForegroundColor(LogRecord.Priority priority) {
        return switch (priority) {
            case VERBOSE -> new Color(0x000000);
            case DEBUG -> new Color(0x0000DD);
            case INFO -> new Color(0x228B22);
            case WARN -> new Color(0xFF681F);
            case ERROR, FATAL -> new Color(0xFF0000);
        };
    }

    @Override
    public Color getBackgroundColor() {
        return new Color(0xFFFFFF);
    }

    @Override
    public Color getBookmarkBackgroundColor() {
        return new Color(0x01146D);
    }

    @Override
    public Color getBookmarkForegroundColor() {
        return new Color(0xF4EE00);
    }

    @Override
    public Font configureBookmarkFont(Font baseFont) {
        return baseFont;
    }
}

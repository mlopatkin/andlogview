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

import name.mlopatkin.andlogview.base.annotations.Contract;
import name.mlopatkin.andlogview.logmodel.LogRecord;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import com.google.gson.JsonSyntaxException;

import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link ThemeColors} based on JSON theme definition.
 */
public class JsonBasedThemeColors implements ThemeColors {
    private final Color logTableBackground;
    private final Color logTableBookmarksBackground;
    private final Color logTableBookmarksForeground;
    private final ThemeColorsJson.@Nullable FontStyle logTableBookmarkFont;
    private final ImmutableMap<LogRecord.Priority, Color> logTablePriorityForeground;
    private final ImmutableList<Color> logTableHighlightBackground;

    private JsonBasedThemeColors(ThemeColorsJson data) {
        // We convert the JSON-based representation into stuff that ThemeColors provide, simultaneously verifying the
        // invariants.
        checkArgument(data.logTable() != null, "logTable is missing");
        var logTable = data.logTable();

        checkArgument(logTable.background() != null, "logTable.background is missing");
        this.logTableBackground = logTable.background();

        checkArgument(logTable.bookmarks() != null, "logTable.bookmarks is missing");
        checkArgument(logTable.bookmarks().background() != null, "logTable.bookmarks.background is missing");
        checkArgument(logTable.bookmarks().foreground() != null, "logTable.bookmarks.foreground is missing");
        this.logTableBookmarksBackground = logTable.bookmarks().background();
        this.logTableBookmarksForeground = logTable.bookmarks().foreground();
        this.logTableBookmarkFont = logTable.bookmarks().fontStyle();

        ImmutableMap.Builder<LogRecord.Priority, Color> priorityForegroundBuilder = ImmutableMap.builder();

        checkArgument(logTable.priority() != null, "logTable.priority is missing");
        for (var p : LogRecord.Priority.values()) {
            var priorityColor = logTable.priority().get(p);
            checkArgument(priorityColor != null, "logTable.priority.%s is missing", p.name());
            checkArgument(priorityColor.foreground() != null, "logTable.priority.%s.foreground is missing", p.name());

            priorityForegroundBuilder.put(p, priorityColor.foreground());
        }
        logTablePriorityForeground = priorityForegroundBuilder.build();

        ImmutableList.Builder<Color> highlightsBackgroundBuilder = ImmutableList.builder();
        checkArgument(logTable.highlights() != null, "logTable.highlights is missing");
        checkArgument(!logTable.highlights().isEmpty(), "logTable.highlights is empty");
        for (int i = 0; i < logTable.highlights().size(); i++) {
            var clr = logTable.highlights().get(i);
            checkArgument(clr != null, "logTable.highlights[%s] is missing", i);
            checkArgument(clr.background() != null, "logTable.highlights[%s].background is missing", i);
            highlightsBackgroundBuilder.add(clr.background());
        }
        this.logTableHighlightBackground = highlightsBackgroundBuilder.build();
    }

    @Override
    public List<Color> getHighlightColors() {
        return logTableHighlightBackground;
    }

    @Override
    public Color getPriorityForegroundColor(LogRecord.Priority priority) {
        return Objects.requireNonNull(logTablePriorityForeground.get(priority),
                () -> "Foreground color for priority " + priority.name() + " is missing");
    }

    @Override
    public Color getBackgroundColor() {
        return logTableBackground;
    }

    @Override
    public Color getBookmarkBackgroundColor() {
        return logTableBookmarksBackground;
    }

    @Override
    public Color getBookmarkForegroundColor() {
        return logTableBookmarksForeground;
    }

    @Override
    public Font configureBookmarkFont(Font baseFont) {
        if (logTableBookmarkFont == null) {
            return baseFont;
        }
        return switch (logTableBookmarkFont) {
            case BOLD -> baseFont.deriveFont(Font.BOLD);
            case REGULAR -> baseFont.deriveFont(Font.PLAIN);
        };
    }

    @FormatMethod
    @Contract("false, _, _ -> fail")
    private static void checkArgument(boolean condition, @FormatString String message, Object... args) {
        if (!condition) {
            throw new JsonSyntaxException(message.formatted(args));
        }
    }

    /**
     * Builds an instance based on the definition of the theme. All required data must be provided.
     *
     * @param data the parsed theme data
     * @return the full set of theme colors
     */
    public static JsonBasedThemeColors fromThemeDefinition(ThemeColorsJson data) throws JsonSyntaxException {
        return new JsonBasedThemeColors(data);
    }
}

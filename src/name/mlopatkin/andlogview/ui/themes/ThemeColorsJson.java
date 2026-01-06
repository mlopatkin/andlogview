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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A definition of the AndLogView-specific colors. This corresponds to a JSON file with theme values.
 * <p>
 * This class may be used to parse complete themes, where {@code null} values are not expected in general, or theme
 * overrides, where only a subset of values is provided. This is the reason for so many nullable types in there.
 * <p>
 * Having {@code null} fields is also important for serialization, to skip them from the serialized representation.
 * <p>
 * All data in this class is immutable.
 *
 * @param logTable the colors used in the log table
 */
public record ThemeColorsJson(@Nullable LogTable logTable) {
    /**
     * Merges this theme with {@code override}. In general, non-null values from overriding instance take over values in
     * this object.
     *
     * @param override the overriding object, can be null to skip overriding
     * @return the new theme definition with overriding values applied
     */
    ThemeColorsJson merge(@Nullable ThemeColorsJson override) {
        if (override == null) {
            return this;
        }
        if (logTable == null) {
            return override;
        }
        return new ThemeColorsJson(logTable.merge(override.logTable()));
    }

    /**
     * A set of colors applied to the log table.
     *
     * @param background the default background of table rows. Mandatory in the theme.
     * @param bookmarks the color scheme of a bookmarked row. Mandatory in the theme.
     * @param priority the coloring of rows based on priority. Foreground is mandatory in the theme for every
     *         priority. Overriding happens per-priority, per color element (e.g. you can override just the foreground
     *         for {@code MAIN}).
     * @param highlights the coloring of highlight filters. At least one must be present. Only background is
     *         mandatory in the theme. When overriding, the whole list is overridden.
     */
    public record LogTable(
            @Nullable Color background,
            @Nullable RowStyle bookmarks,
            Map<LogRecord.Priority, RowStyle> priority,
            @Nullable List<RowStyle> highlights
    ) {
        // The public constructor reflects the reality of the JSON parsing, where everything is possible.
        public LogTable(
                @Nullable Color background,
                @Nullable RowStyle bookmarks,
                @Nullable Map<LogRecord.@Nullable Priority, @Nullable RowStyle> priority,
                @Nullable List<@Nullable RowStyle> highlights
        ) {
            this.background = background;
            this.bookmarks = bookmarks;
            this.priority = sanitize(priority);
            this.highlights = sanitize(highlights);
        }

        // Solves NullAway complaint about List<T?> not being a supertype of List<T!> when calling constructor with a
        // list of non-nullables. As our lists and maps are read-only, we can cast them safely.
        @SuppressWarnings("unchecked")
        public static LogTable create(
                @Nullable Color background,
                @Nullable RowStyle bookmarks,
                @Nullable Map<? extends LogRecord.@Nullable Priority, ? extends @Nullable RowStyle> priority,
                @Nullable List<? extends @Nullable RowStyle> highlights
        ) {
            return new LogTable(
                    background,
                    bookmarks,
                    (Map<LogRecord.@Nullable Priority, @Nullable RowStyle>) priority,
                    (List<@Nullable RowStyle>) highlights
            );
        }


        private static Map<LogRecord.Priority, RowStyle> sanitize(
                @Nullable Map<LogRecord.@Nullable Priority, @Nullable RowStyle> priority) {
            if (priority == null) {
                return Map.of();
            }
            // Can't use contains{Key,Value}(null) because of the null-hostility
            Preconditions.checkArgument(
                    priority.entrySet().stream().noneMatch(e -> e.getKey() == null || e.getValue() == null),
                    "logTable.priority should not contain nulls"
            );
            // noinspection RedundantCast - NullAway
            return (Map<LogRecord.@NonNull Priority, @NonNull RowStyle>) priority;
        }

        private static @Nullable List<RowStyle> sanitize(@Nullable List<@Nullable RowStyle> r) {
            // Can't use contains(null) because of the null-hostility
            Preconditions.checkArgument(
                    r == null || r.stream().noneMatch(Objects::isNull),
                    "logTable.highlights contains null element"
            );
            // noinspection RedundantCast - NullAway
            return (List<@NonNull RowStyle>) r;
        }

        private LogTable merge(@Nullable LogTable other) {
            if (other == null) {
                return this;
            }
            var newBackground = mergeValue(background, other.background);
            var newBookmarks = RowStyle.merge(bookmarks, other.bookmarks);
            var newPriority = mergePriority(other.priority);
            var newHighlights = mergeHighlights(other.highlights);

            return LogTable.create(newBackground, newBookmarks, newPriority, newHighlights);
        }

        private Map<LogRecord.Priority, RowStyle> mergePriority(Map<LogRecord.Priority, RowStyle> other) {
            if (other.isEmpty()) {
                return this.priority;
            }
            if (this.priority.isEmpty()) {
                return other;
            }

            assert !other.isEmpty() && !this.priority.isEmpty();

            var priorityMap = ImmutableMap.<LogRecord.Priority, RowStyle>builder();
            for (var priority : LogRecord.Priority.values()) {
                var baseColors = this.priority.get(priority);
                var overlayColors = other.get(priority);

                var resultColors = RowStyle.merge(baseColors, overlayColors);
                if (resultColors != null) {
                    priorityMap.put(priority, resultColors);
                }
            }
            return priorityMap.build();
        }

        private @Nullable List<RowStyle> mergeHighlights(@Nullable List<RowStyle> other) {
            return other != null ? other : this.highlights;
        }
    }

    /**
     * A representation of the style of a row or a cell in the log table. In general, {@code null} value means that it
     * is inherited from container's defaults.
     *
     * @param background the background color of the cell/row
     * @param foreground the color of the text in the cell/row
     * @param fontStyle the optional font style for the text, e.g. bold or italic.
     */
    public record RowStyle(
            @Nullable Color background,
            @Nullable Color foreground,
            @Nullable FontStyle fontStyle
    ) {
        public RowStyle(@Nullable Color background, @Nullable Color foreground) {
            this(background, foreground, null);
        }

        private static @Nullable RowStyle merge(@Nullable RowStyle base, @Nullable RowStyle overlay) {
            if (overlay == null) {
                return base;
            }
            if (base == null) {
                return overlay;
            }

            assert base != null;
            assert overlay != null;

            return new RowStyle(
                    mergeValue(base.background, overlay.background),
                    mergeValue(base.foreground, overlay.foreground),
                    mergeValue(base.fontStyle, overlay.fontStyle)
            );
        }

        public static RowStyle background(Color background) {
            return new RowStyle(background, null, null);
        }
        public static RowStyle foreground(Color foreground) {
            return new RowStyle(null, foreground, null);
        }
    }

    public enum FontStyle {
        BOLD,
        REGULAR
    }

    private static @Nullable <T> T mergeValue(@Nullable T color, @Nullable T overlay) {
        return overlay != null ? overlay : color;
    }
}

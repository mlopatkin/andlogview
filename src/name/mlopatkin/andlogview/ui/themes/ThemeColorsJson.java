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

import com.google.common.collect.ImmutableMap;

import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.List;
import java.util.Map;

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
            @Nullable RowColors bookmarks,
            @Nullable Map<LogRecord.Priority, @Nullable RowColors> priority,
            @Nullable List<@Nullable RowColors> highlights
    ) {
        // Solves NullAway complaint about List<T?> not being a supertype of List<T!> when calling constructor with a
        // list of non-nullables. As our lists and maps are read-only, we can cast them safely.
        public static LogTable create(
                @Nullable Color background,
                @Nullable RowColors bookmarks,
                @Nullable Map<LogRecord.Priority, ? extends @Nullable RowColors> priority,
                @Nullable List<? extends @Nullable RowColors> highlights
        ) {
            return new LogTable(background, bookmarks, sanitize(priority), sanitize(highlights));
        }

        @SuppressWarnings({"NullAway", "unchecked"})
        private static @Nullable Map<LogRecord.Priority, @Nullable RowColors> sanitize(
                @Nullable Map<LogRecord.Priority, ? extends @Nullable RowColors> priority
        ) {
            return (Map<LogRecord.Priority, RowColors>) priority;
        }

        @SuppressWarnings({"NullAway", "unchecked"})
        private static @Nullable List<@Nullable RowColors> sanitize(
                @Nullable List<? extends @Nullable RowColors> colors
        ) {
            return (List<RowColors>) colors;
        }

        private LogTable merge(@Nullable LogTable other) {
            if (other == null) {
                return this;
            }
            var newBackground = mergeColor(background, other.background);
            var newBookmarks = RowColors.merge(bookmarks, other.bookmarks);
            var newPriority = mergePriority(other.priority);
            var newHighlights = mergeHighlights(other.highlights);

            return new LogTable(newBackground, newBookmarks, newPriority, newHighlights);
        }

        private @Nullable Map<LogRecord.Priority, @Nullable RowColors> mergePriority(
                @Nullable Map<LogRecord.Priority, @Nullable RowColors> other
        ) {
            if (other == null) {
                return this.priority;
            }
            if (this.priority == null) {
                return other;
            }

            assert other != null && this.priority != null;

            var priorityMap = ImmutableMap.<LogRecord.Priority, RowColors>builder();
            for (var priority : LogRecord.Priority.values()) {
                var baseColors = this.priority.get(priority);
                var overlayColors = other.get(priority);

                var resultColors = RowColors.merge(baseColors, overlayColors);
                if (resultColors != null) {
                    priorityMap.put(priority, resultColors);
                }
            }
            // noinspection RedundantCast - NullAway needs it
            return (Map<LogRecord.Priority, @Nullable RowColors>) priorityMap.build();
        }

        private @Nullable List<@Nullable RowColors> mergeHighlights(@Nullable List<@Nullable RowColors> other) {
            return other != null ? other : this.highlights;
        }
    }

    /**
     * A representation of the color of a row or a cell in the log table. In general, {@code null} color means that it
     * is inherited from container's defaults.
     *
     * @param background the background color of the cell/row
     * @param foreground the color of the text in the cell/row
     */
    public record RowColors(
            @Nullable Color background,
            @Nullable Color foreground
    ) {
        private static @Nullable RowColors merge(@Nullable RowColors base, @Nullable RowColors overlay) {
            if (overlay == null) {
                return base;
            }
            if (base == null) {
                return overlay;
            }

            assert base != null;
            assert overlay != null;

            return new RowColors(
                    mergeColor(base.background, overlay.background),
                    mergeColor(base.foreground, overlay.foreground)
            );
        }
    }

    private static @Nullable Color mergeColor(@Nullable Color color, @Nullable Color overlay) {
        return overlay != null ? overlay : color;
    }
}

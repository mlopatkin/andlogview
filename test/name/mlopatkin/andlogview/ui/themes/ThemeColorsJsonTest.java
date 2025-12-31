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

import static org.assertj.core.api.Assertions.assertThat;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.ui.themes.ThemeColorsJson.LogTable;
import name.mlopatkin.andlogview.ui.themes.ThemeColorsJson.RowColors;

import com.google.common.collect.ImmutableMap;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.List;
import java.util.Map;

class ThemeColorsJsonTest {

    @Test
    void mergeWithNullOverrideReturnsOriginal() {
        var original = new ThemeColorsJson(createLogTable());

        var result = original.merge(null);

        assertThat(result).isEqualTo(original);
    }

    @Test
    void mergeWithNullLogTableReturnsOverride() {
        var base = new ThemeColorsJson(null);
        var override = new ThemeColorsJson(createLogTable());

        var result = base.merge(override);

        assertThat(result).isEqualTo(override);
    }

    @Test
    void mergeLogTableMergesNestedFields() {
        var baseTable = LogTable.create(
                Color.WHITE,
                new RowColors(Color.YELLOW, Color.BLACK),
                mapOf(LogRecord.Priority.ERROR, new RowColors(Color.RED, Color.WHITE)),
                List.of(new RowColors(Color.BLUE, Color.WHITE))
        );

        var overrideTable = LogTable.create(
                Color.GRAY,
                null,
                mapOf(LogRecord.Priority.WARN, new RowColors(Color.ORANGE, Color.BLACK)),
                List.of(new RowColors(Color.GREEN, Color.BLACK))
        );

        var logTable = merge(baseTable, overrideTable);

        assertThat(logTable).isNotNull();
        assertThat(logTable.background()).isEqualTo(Color.GRAY);
        assertThat(logTable.bookmarks()).isEqualTo(new RowColors(Color.YELLOW, Color.BLACK));
        assertThat(logTable.priority()).containsEntry(
                LogRecord.Priority.ERROR,
                new RowColors(Color.RED, Color.WHITE)
        );
        assertThat(logTable.priority()).containsEntry(
                LogRecord.Priority.WARN,
                new RowColors(Color.ORANGE, Color.BLACK)
        );
        assertThat(logTable.highlights()).isEqualTo(
                List.of(new RowColors(Color.GREEN, Color.BLACK))
        );
    }

    @Test
    void mergeOverridesBackground() {
        var result = merge(
                LogTable.create(Color.WHITE, null, null, null),
                LogTable.create(Color.BLACK, null, null, null)
        );

        assertThat(result).isNotNull();
        assertThat(result.background()).isEqualTo(Color.BLACK);
    }

    @Test
    void mergeKeepsBackgroundWhenOverrideIsNull() {
        var result = merge(
                LogTable.create(Color.WHITE, null, null, null),
                LogTable.create(null, null, null, null)
        );

        assertThat(result).isNotNull();
        assertThat(result.background()).isEqualTo(Color.WHITE);
    }

    @Test
    void mergeOverridesBookmarks() {
        var baseBookmarks = new RowColors(Color.YELLOW, Color.BLACK);
        var overrideBookmarks = new RowColors(Color.CYAN, Color.BLUE);

        var result = merge(
                LogTable.create(null, baseBookmarks, null, null),
                LogTable.create(null, overrideBookmarks, null, null)
        );

        assertThat(result).isNotNull();
        assertThat(result.bookmarks()).isEqualTo(overrideBookmarks);
    }

    @Test
    void mergeKeepsBookmarksWhenOverrideIsNull() {
        var baseBookmarks = new RowColors(Color.YELLOW, Color.BLACK);

        var result = merge(
                LogTable.create(null, baseBookmarks, null, null),
                LogTable.create(null, null, null, null)
        );

        assertThat(result).isNotNull();
        assertThat(result.bookmarks()).isEqualTo(baseBookmarks);
    }

    @Test
    void mergeBookmarksOverridesIndividualColors() {
        var result = merge(
                LogTable.create(null, new RowColors(Color.RED, Color.WHITE), null, null),
                LogTable.create(null, new RowColors(Color.BLUE, null), null, null)
        );

        assertThat(result).isNotNull();
        var bookmarks = result.bookmarks();
        assertThat(bookmarks).isNotNull();
        assertThat(bookmarks.background()).isEqualTo(Color.BLUE);
        assertThat(bookmarks.foreground()).isEqualTo(Color.WHITE);
    }

    @Test
    void mergeBookmarksKeepsColorsWhenOverrideHasNulls() {
        var result = merge(
                LogTable.create(null, new RowColors(Color.RED, Color.WHITE), null, null),
                LogTable.create(null, new RowColors(null, null), null, null)
        );

        assertThat(result).isNotNull();
        assertThat(result.bookmarks()).isEqualTo(new RowColors(Color.RED, Color.WHITE));
    }

    @Test
    void mergePriorityWithNullOverrideReturnsOriginal() {
        var basePriority = mapOf(
                LogRecord.Priority.ERROR, new RowColors(Color.RED, Color.WHITE)
        );

        var result = merge(
                LogTable.create(null, null, basePriority, null),
                LogTable.create(null, null, null, null)
        );

        assertThat(result).isNotNull();
        assertThat(result.priority()).isEqualTo(basePriority);
    }

    @Test
    void mergePriorityWithNullBaseTakesOverride() {
        var overridePriority = mapOf(
                LogRecord.Priority.WARN, new RowColors(Color.ORANGE, Color.BLACK)
        );

        var result = merge(
                LogTable.create(null, null, null, null),
                LogTable.create(null, null, overridePriority, null)
        );

        assertThat(result).isNotNull();
        assertThat(result.priority()).isEqualTo(overridePriority);
    }

    @Test
    void mergePriorityMergesIndividualPriorities() {
        var basePriority = mapOf(
                LogRecord.Priority.ERROR, new RowColors(Color.RED, Color.WHITE),
                LogRecord.Priority.WARN, new RowColors(Color.ORANGE, Color.BLACK)
        );
        var overridePriority = mapOf(
                LogRecord.Priority.WARN, new RowColors(Color.YELLOW, null),
                LogRecord.Priority.INFO, new RowColors(Color.GREEN, Color.WHITE)
        );

        var result = merge(
                LogTable.create(null, null, basePriority, null),
                LogTable.create(null, null, overridePriority, null)
        );

        assertThat(result).isNotNull();
        var priority = result.priority();
        assertThat(priority).isNotNull();
        assertThat(priority).containsEntry(
                LogRecord.Priority.ERROR,
                new RowColors(Color.RED, Color.WHITE)
        );
        assertThat(priority).containsEntry(
                LogRecord.Priority.WARN,
                new RowColors(Color.YELLOW, Color.BLACK)
        );
        assertThat(priority).containsEntry(
                LogRecord.Priority.INFO,
                new RowColors(Color.GREEN, Color.WHITE)
        );
    }

    @Test
    void mergePriorityRowColorsOverridesIndividualColors() {
        var basePriority = mapOf(
                LogRecord.Priority.ERROR, new RowColors(Color.RED, Color.WHITE)
        );
        var overridePriority = mapOf(
                LogRecord.Priority.ERROR, new RowColors(null, Color.BLACK)
        );

        var result = merge(
                LogTable.create(null, null, basePriority, null),
                LogTable.create(null, null, overridePriority, null)
        );

        assertThat(result).isNotNull();
        var priority = result.priority();
        assertThat(priority).isNotNull();
        assertThat(priority).containsEntry(
                LogRecord.Priority.ERROR,
                new RowColors(Color.RED, Color.BLACK)
        );
    }

    @Test
    void mergeHighlightsReplacesCompleteList() {
        var baseHighlights = List.of(
                new RowColors(Color.BLUE, Color.WHITE),
                new RowColors(Color.GREEN, Color.BLACK)
        );
        var overrideHighlights = List.of(
                new RowColors(Color.CYAN, Color.RED)
        );

        var result = merge(
                LogTable.create(null, null, null, baseHighlights),
                LogTable.create(null, null, null, overrideHighlights)
        );

        assertThat(result).isNotNull();
        assertThat(result.highlights()).isEqualTo(overrideHighlights);
    }

    @Test
    void mergeHighlightsKeepsOriginalWhenOverrideIsNull() {
        var baseHighlights = List.of(
                new RowColors(Color.BLUE, Color.WHITE)
        );

        var result = merge(
                LogTable.create(null, null, null, baseHighlights),
                LogTable.create(null, null, null, null)
        );

        assertThat(result).isNotNull();
        assertThat(result.highlights()).isEqualTo(baseHighlights);
    }

    private static LogTable createLogTable() {
        return LogTable.create(
                Color.WHITE,
                new RowColors(Color.YELLOW, Color.BLACK),
                mapOf(),
                List.of()
        );
    }

    private static @Nullable LogTable merge(@Nullable LogTable left, @Nullable LogTable right) {
        return new ThemeColorsJson(left).merge(new ThemeColorsJson(right)).logTable();
    }

    private static Map<LogRecord.Priority, RowColors> mapOf() {
        return ImmutableMap.of();
    }

    private static Map<LogRecord.Priority, RowColors> mapOf(
            LogRecord.Priority k1, RowColors v1
    ) {
        return ImmutableMap.of(k1, v1);
    }

    private static Map<LogRecord.Priority, RowColors> mapOf(
            LogRecord.Priority k1, RowColors v1,
            LogRecord.Priority k2, RowColors v2
    ) {
        return ImmutableMap.of(k1, v1, k2, v2);
    }
}

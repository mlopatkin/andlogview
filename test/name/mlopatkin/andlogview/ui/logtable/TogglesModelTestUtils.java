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

package name.mlopatkin.andlogview.ui.logtable;

import name.mlopatkin.andlogview.test.AdaptingMatcher;

import org.hamcrest.Matcher;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Helper methods to test implementations of {@link ColumnTogglesModel}.
 */
public class TogglesModelTestUtils {
    public static Matcher<ColumnTogglesModel> visibleColumns(Matcher<Iterable<? extends Column>> columnsMatcher) {
        return new AdaptingMatcher<>(TogglesModelTestUtils::getVisibleColumns, columnsMatcher);
    }

    public static Matcher<ColumnTogglesModel> availableColumns(Matcher<Iterable<? extends Column>> columnsMatcher) {
        return new AdaptingMatcher<>(TogglesModelTestUtils::getAvailableColumns, columnsMatcher);
    }

    public static Set<Column> getVisibleColumns(ColumnTogglesModel prefs) {
        return getMatchingColumns(prefs::isColumnVisible);
    }

    public static Set<Column> getAvailableColumns(ColumnTogglesModel prefs) {
        return getMatchingColumns(prefs::isColumnAvailable);
    }

    public static Set<Column> getMatchingColumns(Predicate<Column> columnPredicate) {
        return Arrays.stream(Column.values()).filter(columnPredicate).collect(Collectors.toSet());
    }
}

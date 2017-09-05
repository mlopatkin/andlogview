/*
 * Copyright 2017 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.logtable;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.table.TableColumn;

public final class TableColumnTestUtils {
    private TableColumnTestUtils() {
    }

    public static Matcher<Iterable<? extends TableColumn>> areTableColumnsFor(Column... columns) {
        return areTableColumnsFor(Arrays.asList(columns));
    }

    public static Matcher<Iterable<? extends TableColumn>> areTableColumnsFor(Collection<Column> columns) {
        return Matchers.contains(columns.stream().map(TableColumnTestUtils::isColumnFor).collect(Collectors.toList()));
    }

    public static Matcher<TableColumn> isColumnFor(Column column) {
        return new TypeSafeMatcher<TableColumn>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("is a column for ").appendValue(column);
            }

            @Override
            protected boolean matchesSafely(TableColumn item) {
                return Column.getByColumnIndex(item.getModelIndex()) == column;
            }

            @Override
            protected void describeMismatchSafely(TableColumn item, Description mismatchDescription) {
                mismatchDescription.appendText("was column for ")
                                   .appendValue(Column.getByColumnIndex(item.getModelIndex()));
            }
        };
    }
}

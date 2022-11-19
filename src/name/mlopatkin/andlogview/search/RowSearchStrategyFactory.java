/*
 * Copyright 2013 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.search;

import name.mlopatkin.andlogview.search.text.HighlightStrategy;
import name.mlopatkin.andlogview.search.text.SearchStrategyFactory;
import name.mlopatkin.andlogview.ui.logtable.Column;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Grammar:
 *
 * <pre>
 * valid_symbol := [^:/]|'\:'|'\/'
 * nonspace := (valid_symbol) that is not a space
 * pattern := (nonspace)(valid_symbol)*(nonspace)
 * regex := /(valid_symbol)* /
 * prefix := app|tag|msg
 * single_simple_query := ((pattern)|(regex))
 * single_qualified_query := (prefix):(single_simple_query)
 * single_query := single_simple_query | single_qualified_query
 * </pre>
 */
public final class RowSearchStrategyFactory {
    private RowSearchStrategyFactory() {}

    private static final String PREFIX_APP = "app:";
    private static final String PREFIX_TAG = "tag:";
    private static final String PREFIX_MSG = "msg:";

    private static final List<String> PREFIXES = Arrays.asList(PREFIX_APP, PREFIX_MSG, PREFIX_TAG);

    public static @Nullable RowSearchStrategy compile(@Nullable String pattern) throws RequestCompilationException {
        if (CharMatcher.whitespace().matchesAllOf(Strings.nullToEmpty(pattern))) {
            return null;
        }
        assert pattern != null;
        pattern = pattern.trim();
        // check for prefix
        for (String prefix : PREFIXES) {
            if (pattern.startsWith(prefix)) {
                return prepareWithPrefix(prefix, pattern.substring(prefix.length()));
            }
        }
        return prepareWithoutPrefix(pattern);
    }

    private static RowSearchStrategy prepareWithPrefix(String prefix, String rest) throws RequestCompilationException {
        rest = rest.trim();
        HighlightStrategy strategy = SearchStrategyFactory.createHighlightStrategy(rest);

        if (PREFIX_APP.equals(prefix)) {
            return new ValueSearcher(strategy, Column.APP_NAME);
        } else if (PREFIX_MSG.equals(prefix)) {
            return new ValueSearcher(strategy, Column.MESSAGE);
        } else if (PREFIX_TAG.equals(prefix)) {
            return new ValueSearcher(strategy, Column.TAG);
        } else {
            throw new AssertionError("Unrecognized prefix");
        }
    }

    private static RowSearchStrategy prepareWithoutPrefix(String pattern) throws RequestCompilationException {
        HighlightStrategy strategy = SearchStrategyFactory.createHighlightStrategy(pattern);
        Column[] searchableColumns = {Column.APP_NAME, Column.MESSAGE, Column.TAG};
        List<ValueSearcher> searches = new ArrayList<>(searchableColumns.length);
        for (Column c : searchableColumns) {
            searches.add(new ValueSearcher(strategy, c));
        }
        return new OrSearcher(searches);
    }
}

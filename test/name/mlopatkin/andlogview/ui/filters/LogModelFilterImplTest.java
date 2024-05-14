/*
 * Copyright 2024 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.filters;

import static org.assertj.core.api.Assertions.assertThat;

import name.mlopatkin.andlogview.filters.ColoringToggleFilter;
import name.mlopatkin.andlogview.filters.Filter;
import name.mlopatkin.andlogview.filters.FilterModel;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.filters.ToggleFilter;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecordPredicates;
import name.mlopatkin.andlogview.logmodel.LogRecordUtils;
import name.mlopatkin.andlogview.ui.logtable.LogModelFilter;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.Arrays;
import java.util.function.Predicate;

class LogModelFilterImplTest {
    @Test
    void loadsShowFiltersFromInitialState() {
        var filter = create(createModel(show(matchesTag("ActivityManager"))));

        assertThat(filter.shouldShowRecord(recordWithTag("ActivityManager"))).isTrue();
        assertThat(filter.shouldShowRecord(recordWithTag("PackageManager"))).isFalse();
    }

    @Test
    void loadsDisabledShowFiltersFromInitialState() {
        var filter = create(createModel(show(matchesTag("ActivityManager")).disabled()));

        assertThat(filter.shouldShowRecord(recordWithTag("ActivityManager"))).isTrue();
        assertThat(filter.shouldShowRecord(recordWithTag("PackageManager"))).isTrue();
    }

    @Test
    void loadsHideFiltersFromInitialState() {
        var filter = create(createModel(hide(matchesTag("ActivityManager"))));

        assertThat(filter.shouldShowRecord(recordWithTag("ActivityManager"))).isFalse();
        assertThat(filter.shouldShowRecord(recordWithTag("PackageManager"))).isTrue();
    }

    @Test
    void loadsDisabledHideFiltersFromInitialState() {
        var filter = create(createModel(hide(matchesTag("ActivityManager")).disabled()));

        assertThat(filter.shouldShowRecord(recordWithTag("ActivityManager"))).isTrue();
        assertThat(filter.shouldShowRecord(recordWithTag("PackageManager"))).isTrue();
    }

    @Test
    void loadsColorFiltersFromInitialState() {
        var filter = create(createModel(color(Color.BLACK, matchesTag("ActivityManager"))));

        assertThat(filter.getHighlightColor(recordWithTag("ActivityManager"))).isEqualTo(Color.BLACK);
        assertThat(filter.getHighlightColor(recordWithTag("PackageManager"))).isNull();
    }

    @Test
    void loadsDisabledColorFiltersFromInitialState() {
        var filter = create(createModel(color(Color.BLACK, matchesTag("ActivityManager")).disabled()));

        assertThat(filter.getHighlightColor(recordWithTag("ActivityManager"))).isNull();
        assertThat(filter.getHighlightColor(recordWithTag("PackageManager"))).isNull();
    }

    @Test
    void skipsUnsupportedFiltersUponLoad() {
        var filter = create(createModel(index(matchesTag("ActivityManager"))));

        assertThat(filter.shouldShowRecord(recordWithTag("ActivityManager"))).isTrue();
        assertThat(filter.shouldShowRecord(recordWithTag("PackageManager"))).isTrue();
        assertThat(filter.getHighlightColor(recordWithTag("ActivityManager"))).isNull();
        assertThat(filter.getHighlightColor(recordWithTag("PackageManager"))).isNull();
    }

    private Filter show(Predicate<? super LogRecord> predicate) {
        return new ToggleFilter(FilteringMode.SHOW, true, predicate);
    }

    private Filter hide(Predicate<? super LogRecord> predicate) {
        return new ToggleFilter(FilteringMode.HIDE, true, predicate);
    }

    private Filter color(Color color, Predicate<? super LogRecord> predicate) {
        return new ColoringToggleFilter(color, true, predicate);
    }

    private Filter index(Predicate<? super LogRecord> predicate) {
        return new ToggleFilter(FilteringMode.WINDOW, true, predicate);
    }

    private FilterModel createModel(Filter... filters) {
        return FilterModel.create(Arrays.asList(filters));
    }

    private LogModelFilter create(FilterModel model) {
        return new LogModelFilterImpl(model);
    }

    private LogRecord recordWithTag(String tag) {
        return LogRecordUtils.forTag(tag);
    }

    private Predicate<LogRecord> matchesTag(String tag) {
        return LogRecordPredicates.matchTag(tag::equals);
    }
}

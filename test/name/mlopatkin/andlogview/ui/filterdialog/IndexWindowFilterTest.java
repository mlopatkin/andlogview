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

package name.mlopatkin.andlogview.ui.filterdialog;

import static name.mlopatkin.andlogview.filters.ToggleFilter.show;
import static name.mlopatkin.andlogview.logmodel.LogRecordUtils.forTag;

import static org.assertj.core.api.Assertions.assertThat;

import name.mlopatkin.andlogview.filters.FilterChain;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.utils.Try;

import com.google.common.collect.ImmutableList;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

class IndexWindowFilterTest {
    @Test
    void modelShowsOnlyMatchingRecords() {
        var filter = createFilterForTags("ActivityManager");
        assertThat(asPredicate(filter))
                .accepts(forTag("ActivityManager"))
                .rejects(forTag("PackageManager"));
    }

    @Test
    void nonMatchingRecordsCannotReappearBecauseOfChildFilters() {
        var filter = createFilterForTags("ActivityManager");
        filter.getChildren()
                .addFilter(show(r -> true));
        assertThat(asPredicate(filter))
                .accepts(forTag("ActivityManager"))
                .rejects(forTag("PackageManager"));
    }

    private IndexWindowFilter createFilterForTags(String... tags) {
        return Try.ofCallable(
                        () -> new IndexWindowFilter(true,
                                new FilterFromDialogData(FilteringMode.WINDOW)
                                        .setTags(ImmutableList.copyOf(tags))))
                .get();
    }

    private Predicate<LogRecord> asPredicate(IndexWindowFilter filter) {
        var filterChain = new FilterChain(filter.getChildren());
        return filterChain::shouldShow;
    }
}

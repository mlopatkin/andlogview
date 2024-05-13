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

package name.mlopatkin.andlogview.ui.indexfilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.ui.logtable.LogModelFilter;
import name.mlopatkin.andlogview.utils.events.Subject;

import org.junit.jupiter.api.Test;

class IndexFilterTest {

    @Test
    void unsubscribesAfterClose() {
        var parent = mock(LogModelFilter.class);
        var subject = new Subject<LogModelFilter.Observer>();
        when(parent.asObservable()).thenReturn(subject.asObservable());

        try (var ignored = new IndexFilter(parent, r -> true)) {
            assertThat(subject).isNotEmpty();
        }

        assertThat(subject).isEmpty();
    }
}

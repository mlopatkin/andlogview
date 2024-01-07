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

package name.mlopatkin.andlogview.filters;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilterModelTest {

    @Mock
    FilterModel.Observer observer;

    @Test
    void addingFilterNotifiesObservers() {
        var model = createModel();
        var filter = createFilter();

        model.asObservable().addObserver(observer);
        model.addFilter(filter);

        verify(observer).onFilterAdded(filter);
    }

    @Test
    void addingFilterTheSecondTimeDoesNotNotifyObservers() {
        var model = createModel();
        var filter = createFilter();
        model.addFilter(filter);

        model.asObservable().addObserver(observer);
        model.addFilter(filter);

        verify(observer, never()).onFilterAdded(filter);
    }

    @Test
    void removingFilterNotifiesObservers() {
        var model = createModel();
        var filter = createFilter();
        model.addFilter(filter);

        model.asObservable().addObserver(observer);
        model.removeFilter(filter);

        verify(observer).onFilterRemoved(filter);
    }

    @Test
    void removingNonAddedFilterDoesNotNotifyObservers() {
        var model = createModel();
        var filter = createFilter();

        model.asObservable().addObserver(observer);
        model.removeFilter(filter);

        verify(observer, never()).onFilterRemoved(filter);
    }

    @Test
    void replacingFilterNotifiesObserver() {
        var model = createModel();
        var filter = createFilter();
        var newFilter = createFilter();
        model.addFilter(filter);

        model.asObservable().addObserver(observer);
        model.replaceFilter(filter, newFilter);

        verify(observer).onFilterReplaced(filter, newFilter);
    }

    @Test
    void replacingFilterWithItselfDoesNothingAndDoesNotNotify() {
        var model = createModel();
        var filter = createFilter();
        model.addFilter(filter);

        model.asObservable().addObserver(observer);
        model.replaceFilter(filter, filter);

        verifyNoInteractions(observer);
    }

    @Test
    void replacingFilterWithExistingThrows() {
        var model = createModel();
        var filter = createFilter();
        var otherFilter = createFilter();
        model.addFilter(filter);
        model.addFilter(otherFilter);

        assertThatThrownBy(() -> model.replaceFilter(filter, otherFilter)).isInstanceOf(IllegalArgumentException.class);
    }

    FilterModel createModel() {
        return new FilterModelImpl();
    }

    Filter createFilter() {
        return new ToggleFilter(FilteringMode.getDefaultMode(), true, logRecord -> true) {
            @Override
            public boolean equals(Object obj) {
                return this == obj;
            }
        };
    }
}

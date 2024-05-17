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

import java.util.function.Consumer;

/**
 * An implementation of {@link FilterModel.Observer} for uses where only a generic change event is of interest rather
 * than precise change details.
 */
public final class FiltersChangeObserver implements FilterModel.Observer {
    private final Consumer<? super FilterModel> changeObserver;

    public FiltersChangeObserver(Consumer<? super FilterModel> changeObserver) {
        this.changeObserver = changeObserver;
    }

    @Override
    public void onFilterAdded(FilterModel model, Filter newFilter) {
        changeObserver.accept(model);
    }

    @Override
    public void onFilterRemoved(FilterModel model, Filter removedFilter) {
        changeObserver.accept(model);
    }

    @Override
    public void onFilterReplaced(FilterModel model, Filter oldFilter, Filter newFilter) {
        changeObserver.accept(model);
    }
}

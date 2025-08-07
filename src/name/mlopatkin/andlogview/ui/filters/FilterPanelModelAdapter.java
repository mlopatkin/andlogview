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

import name.mlopatkin.andlogview.filters.FilterModel;
import name.mlopatkin.andlogview.filters.MutableFilterModel;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filterpanel.FilterPanel;
import name.mlopatkin.andlogview.ui.filterpanel.FilterPanelModel;
import name.mlopatkin.andlogview.utils.events.Subject;

import javax.inject.Inject;

/**
 * Adapts {@link MutableFilterModel} to be used in {@link FilterPanel}.
 */
class FilterPanelModelAdapter extends BaseFilterModelAdapter<PanelFilter> implements FilterPanelModel<PanelFilter> {
    private final Subject<FilterPanelModel.FilterPanelModelListener<? super PanelFilter>> listeners = new Subject<>();

    @Inject
    @SuppressWarnings("NullAway")
    FilterPanelModelAdapter(FilterModel model, PanelFilter.Factory panelFilterFactory) {
        super(model, filter -> {
            if (filter instanceof FilterFromDialog filterFromDialog) {
                return panelFilterFactory.create(filterFromDialog);
            }
            return null;
        });
    }

    @Override
    protected void addFilter(PanelFilter filter) {
        for (var listener : listeners) {
            listener.onFilterAdded(filter);
        }
    }

    @Override
    protected void removeFilter(PanelFilter filter) {
        for (var listener : listeners) {
            listener.onFilterRemoved(filter);
        }
    }

    @Override
    protected void replaceFilter(PanelFilter oldFilter, PanelFilter newFilter) {
        for (var listener : listeners) {
            listener.onFilterReplaced(oldFilter, newFilter);
        }
    }

    @Override
    public void setFilterEnabled(PanelFilter filter, boolean enabled) {
        filter.setEnabled(enabled);
    }

    @Override
    public void removeFilterForView(PanelFilter filter) {
        filter.delete();
    }

    @Override
    public void addListener(FilterPanelModelListener<? super PanelFilter> listener) {
        listeners.asObservable().addObserver(listener);
    }

    @Override
    public void editFilter(PanelFilter filter) {
        filter.openFilterEditor();
    }
}

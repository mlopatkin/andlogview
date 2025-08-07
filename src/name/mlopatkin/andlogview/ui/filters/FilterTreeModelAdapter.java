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

import name.mlopatkin.andlogview.filters.Filter;
import name.mlopatkin.andlogview.filters.MutableFilterModel;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filtertree.FilterTreeModel;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import javax.inject.Inject;

class FilterTreeModelAdapter extends BaseFilterModelAdapter<TreeNodeFilter> implements FilterTreeModel<TreeNodeFilter> {
    private final Subject<ModelObserver<? super TreeNodeFilter>> observers = new Subject<>();
    private final MutableFilterModel model;

    @Inject
    @SuppressWarnings("NullAway")
    FilterTreeModelAdapter(MutableFilterModel model, TreeNodeFilter.Factory nodeFactory) {
        super(model, (Filter f) -> {
            if (f instanceof FilterFromDialog filterFromDialog) {
                return nodeFactory.create(filterFromDialog);
            }
            return null;
        });
        this.model = model;
    }

    @Override
    public void moveFilter(TreeNodeFilter node, int newPosition) {
        var filters = getFilters();
        var before = newPosition < filters.size() ? filters.get(newPosition).getFilter() : null;
        var movingFilter = node.getFilter();
        if (before != movingFilter) {
            model.insertFilterBefore(movingFilter, before);
        }
    }

    @Override
    protected void addFilter(TreeNodeFilter filter) {
        for (var observer : observers) {
            observer.onFilterAdded(filter);
        }
    }

    @Override
    protected void removeFilter(TreeNodeFilter filter) {
        for (var observer : observers) {
            observer.onFilterRemoved(filter);
        }
    }

    @Override
    protected void replaceFilter(TreeNodeFilter oldFilter, TreeNodeFilter newFilter) {
        for (var observer : observers) {
            observer.onFilterReplaced(oldFilter, newFilter);
        }
    }

    @Override
    public void setFilterEnabled(TreeNodeFilter filter, boolean enabled) {
        filter.setEnabled(enabled);
    }

    @Override
    public Observable<ModelObserver<? super TreeNodeFilter>> asObservable() {
        return observers.asObservable();
    }

    @Override
    public void removeFilterForView(TreeNodeFilter filter) {
        model.removeFilter(filter.getFilter());
    }

    @Override
    public void editFilter(TreeNodeFilter filter) {
        filter.openFilterEditor();
    }
}

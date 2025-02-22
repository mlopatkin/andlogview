/*
 * Copyright 2015 Mikhail Lopatkin
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
import name.mlopatkin.andlogview.ui.filterpanel.FilterCreator;
import name.mlopatkin.andlogview.ui.filterpanel.FilterPanel;
import name.mlopatkin.andlogview.ui.filtertree.FilterNodeViewModel;
import name.mlopatkin.andlogview.ui.filtertree.FilterTreeModel;
import name.mlopatkin.andlogview.ui.filtertree.TreeModelAdapter;
import name.mlopatkin.andlogview.ui.logtable.LogModelFilter;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.ui.themes.Theme;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Provides LogModelFilter that can be used with MainFilterController.
 */
@Module
public abstract class FilterModule {
    @Binds
    @MainFrameScoped
    abstract LogModelFilter getModelFilter(LogModelFilterImpl impl);

    @Provides
    @MainFrameScoped
    static BufferFilterModel bufferFilterModel() {
        return new BufferFilterModel(MutableFilterModel.create());
    }

    @Provides
    @MainFrameScoped
    static MutableFilterModel getFiltersModel(StoredFilters storedFilters) {
        return storedFilters.getStorageBackedModel();
    }

    @Binds
    abstract FilterModel filterModel(MutableFilterModel model);

    @Provides
    @MainFrameScoped
    static FilterPanel filterPanel(Theme theme, FilterPanelModelAdapter model, FilterCreator filterCreator) {
        return new FilterPanel(theme, model, filterCreator);
    }

    @Provides
    static FilterTreeModel<FilterNodeViewModel> filterTreeModel(FilterTreeModelAdapter modelAdapter) {
        // TODO(mlopatkin) This is actually not type-safe, but the true safety is surprisingly hard to achieve. As long
        //  as the TreeModelAdapter is created with the FilterTreeModelAdapter, it should work fine.
        //  The problem is that the filtertree module may pick up nodes from the tree model adapter and feed them into
        //  FilterTreeModel.
        @SuppressWarnings("UnnecessaryLocalVariable")
        FilterTreeModel<?> model = modelAdapter;

        @SuppressWarnings("unchecked")
        var typedModel = (FilterTreeModel<FilterNodeViewModel>) model;
        return typedModel;
    }

    @Provides
    @MainFrameScoped
    static TreeModelAdapter treeModelAdapter(FilterTreeModel<FilterNodeViewModel> modelAdapter) {
        return new TreeModelAdapter(modelAdapter);
    }
}

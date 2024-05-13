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
import name.mlopatkin.andlogview.ui.filterpanel.FilterCreator;
import name.mlopatkin.andlogview.ui.filterpanel.FilterPanel;
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
    static FilterModel getFiltersModel(StoredFilters filters) {
        return filters.getStorageBackedModel();
    }

    @Provides
    @MainFrameScoped
    static FilterPanel filterPanel(Theme theme, FilterPanelModelAdapter model, FilterCreator filterCreator) {
        return new FilterPanel(theme, model, filterCreator);
    }
}

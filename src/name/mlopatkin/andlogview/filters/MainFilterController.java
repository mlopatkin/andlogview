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

package name.mlopatkin.andlogview.filters;

import name.mlopatkin.andlogview.ui.filterdialog.FilterDialogFactory;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialogData;
import name.mlopatkin.andlogview.ui.filterpanel.FilterCreator;
import name.mlopatkin.andlogview.ui.indexfilter.IndexFilterCollection;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.ui.mainframe.popupmenu.MenuFilterCreator;
import name.mlopatkin.andlogview.utils.MyFutures;

import javax.inject.Inject;

/**
 * The filter controller of the main window.
 */
@MainFrameScoped
public class MainFilterController implements FilterCreator, MenuFilterCreator {
    // TODO(mlopatkin): Find a new name and place for this class.

    private final FilterDialogFactory dialogFactory;
    private final MutableFilterModel filterModel;

    @Inject
    MainFilterController(
            // TODO(mlopatkin): figure out how to bootstrap these ignored folks. Nothing really depends on them, so
            //  nothing causes them to be created.
            IndexFilterCollection ignoredIndexFilters,
            FilterDialogFactory dialogFactory,
            MutableFilterModel filterModel
    ) {
        this.dialogFactory = dialogFactory;
        this.filterModel = filterModel;
    }

    @Override
    public void createFilterWithDialog() {
        dialogFactory.startCreateFilterDialog()
                .thenAccept(newFilter -> newFilter.ifPresent(this::addFilter))
                .exceptionally(MyFutures::uncaughtException);
    }

    @Override
    public void addFilter(FilterFromDialogData filter) {
        filterModel.addFilter(filter.toFilter());
    }

    @Override
    public void createFilterWithDialog(FilterFromDialogData baseData) {
        dialogFactory.startCreateFilterDialogWithInitialData(baseData)
                .thenAccept(result -> result.ifPresent(this::addFilter))
                .exceptionally(MyFutures::uncaughtException);
    }
}

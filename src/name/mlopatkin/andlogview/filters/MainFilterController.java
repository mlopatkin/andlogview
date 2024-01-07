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
import name.mlopatkin.andlogview.ui.filterdialog.FilterDialogHandle;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filterpanel.FilterCreator;
import name.mlopatkin.andlogview.ui.filterpanel.FilterPanelModel;
import name.mlopatkin.andlogview.ui.filterpanel.PanelFilter;
import name.mlopatkin.andlogview.ui.indexfilter.IndexFilterCollection;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.ui.mainframe.popupmenu.MenuFilterCreator;
import name.mlopatkin.andlogview.utils.MyFutures;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.inject.Inject;

/**
 * The filter controller of the main window.
 */
@MainFrameScoped
public class MainFilterController implements FilterCreator, MenuFilterCreator {
    private final FilterPanelModel filterPanelModel;
    private final FilterDialogFactory dialogFactory;

    private final List<BaseToggleFilter<?>> filters = new ArrayList<>();
    private final FilterModel filterModel;

    @Inject
    MainFilterController(
            FilterPanelModel filterPanelModel,
            IndexFilterCollection indexFilterCollection,
            FilterDialogFactory dialogFactory,
            FilterModel filterModel
    ) {
        this.filterPanelModel = filterPanelModel;
        this.dialogFactory = dialogFactory;
        this.filterModel = filterModel;

        indexFilterCollection.asObservable().addObserver(disabledFilter -> {
            for (BaseToggleFilter<?> registeredFilter : filters) {
                if (Objects.equals(registeredFilter.filter, disabledFilter)) {
                    registeredFilter.setEnabled(false);
                }
            }
        });

        filterModel.asObservable().addObserver(indexFilterCollection.createObserver(Function.identity()));
        for (var filter : filterModel.getFilters()) {
            if (indexFilterCollection.supportsMode(filter.getMode())) {
                indexFilterCollection.addFilter(filter);
            }

            if (filter instanceof FilterFromDialog filterFromDialog) {
                addNewDialogFilter(filterFromDialog);
            }
        }
    }

    @Override
    public void createFilterWithDialog() {
        dialogFactory.startCreateFilterDialog()
                .thenAccept(newFilter -> newFilter.ifPresent(this::addNewDialogFilter))
                .exceptionally(MyFutures::uncaughtException);
    }

    private DialogPanelFilter createDialogPanelFilter(FilterFromDialog filter) {
        return new DialogPanelFilter(filter);
    }

    private DialogPanelFilter addNewDialogFilter(FilterFromDialog filter) {
        DialogPanelFilter dialogPanelFilter = createDialogPanelFilter(filter);
        filters.add(dialogPanelFilter);
        filterPanelModel.addFilter(dialogPanelFilter.addToCollection());
        return dialogPanelFilter;
    }

    @Override
    public void addFilter(FilterFromDialog filter) {
        addNewDialogFilter(filter);
    }

    @Override
    public void createFilterWithDialog(FilterFromDialog baseData) {
        dialogFactory.startCreateFilterDialogWithInitialData(baseData)
                .thenAccept(result -> result.ifPresent(this::addFilter))
                .exceptionally(MyFutures::uncaughtException);
    }

    /**
     * Base class for {@link PanelFilter} wrappers for the DialogFilters and others.
     */
    private abstract class BaseToggleFilter<T extends Filter> implements PanelFilter {
        protected final T filter;

        protected BaseToggleFilter(T filter) {
            this.filter = filter;
        }

        @Override
        public boolean isEnabled() {
            return filter.isEnabled();
        }

        @Override
        public void delete() {
            filterModel.removeFilter(filter);
            filters.remove(this);
        }

        protected void replaceMeWith(BaseToggleFilter<T> replacement) {
            int myPos = filters.indexOf(this);
            if (myPos == -1) {
                // Ignore edit result if |this| filter isn't alive anymore. This can happen if the editor was opened
                // twice for the same filter or if the filter was deleted while editor was open.
                return;
            }
            filters.set(myPos, replacement);
            filterPanelModel.replaceFilter(this, replacement);
            filterModel.replaceFilter(filter, replacement.filter);
        }

        public BaseToggleFilter<T> addToCollection() {
            filterModel.addFilter(filter);
            return this;
        }
    }

    private class DialogPanelFilter extends BaseToggleFilter<FilterFromDialog> {
        private @Nullable FilterDialogHandle dialogHandle;

        protected DialogPanelFilter(FilterFromDialog filter) {
            super(filter);
        }

        @Override
        public void setEnabled(boolean enabled) {
            if (filter.isEnabled() != enabled) {
                replaceMeWith(new DialogPanelFilter(enabled ? filter.enabled() : filter.disabled()));
            }
        }

        @Override
        public void openFilterEditor() {
            FilterDialogHandle currentDialogHandle = dialogHandle;
            if (currentDialogHandle != null) {
                currentDialogHandle.bringToFront();
                return;
            }
            dialogHandle = currentDialogHandle = dialogFactory.startEditFilterDialog(this.filter);
            currentDialogHandle.getResult().thenAccept(newFilterOpt -> {
                dialogHandle = null;
                newFilterOpt.ifPresent(newFilter -> {
                    // Keep enabled status of this filter after editing.
                    newFilter.setEnabled(filter.isEnabled());
                    DialogPanelFilter newPanelFilter = createDialogPanelFilter(newFilter);
                    replaceMeWith(newPanelFilter);
                });
            }).exceptionally(MyFutures::uncaughtException);
        }

        @Override
        public String getTooltip() {
            return filter.getTooltip();
        }
    }

}

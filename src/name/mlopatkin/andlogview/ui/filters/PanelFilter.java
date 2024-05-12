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
import name.mlopatkin.andlogview.ui.filterdialog.FilterDialogFactory;
import name.mlopatkin.andlogview.ui.filterdialog.FilterDialogHandle;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filterpanel.PanelFilterView;
import name.mlopatkin.andlogview.utils.MyFutures;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;

import org.checkerframework.checker.nullness.qual.Nullable;

class PanelFilter implements PanelFilterView {
    private final FilterModel model;
    private final FilterDialogFactory dialogFactory;
    private final FilterFromDialog filter;

    private @Nullable FilterDialogHandle editorHandle;

    @AssistedInject
    public PanelFilter(FilterModel model, FilterDialogFactory dialogFactory, @Assisted FilterFromDialog filter) {
        this.model = model;
        this.dialogFactory = dialogFactory;
        this.filter = filter;
    }

    public void setEnabled(boolean enabled) {
        if (enabled != filter.isEnabled()) {
            model.replaceFilter(filter, enabled ? filter.enabled() : filter.disabled());
        }
    }

    public void openFilterEditor() {
        var editorHandle = this.editorHandle;
        if (editorHandle != null) {
            editorHandle.bringToFront();
            return;
        }
        editorHandle = this.editorHandle = dialogFactory.startEditFilterDialog(filter);
        editorHandle.getResult().thenAccept(optFilter -> {
            optFilter.ifPresent(newFilter -> {
                if (model.getFilters().contains(filter)) {
                    model.replaceFilter(filter, newFilter);
                }
            });
            this.editorHandle = null;
        }).exceptionally(MyFutures::uncaughtException);
    }

    public void delete() {
        model.removeFilter(filter);
    }

    @Override
    public String getTooltip() {
        return filter.getTooltip();
    }

    @Override
    public boolean isEnabled() {
        return filter.isEnabled();
    }

    @AssistedFactory
    interface Factory {
        PanelFilter create(FilterFromDialog filter);
    }
}

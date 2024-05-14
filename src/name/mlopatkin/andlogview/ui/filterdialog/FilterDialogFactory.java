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

package name.mlopatkin.andlogview.ui.filterdialog;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.inject.Provider;

public class FilterDialogFactory {
    private static final String NEW_FILTER_DIALOG_TITLE = "Create new filter";
    private static final String EDIT_FILTER_DIALOG_TITLE = "Edit filter";

    private final Provider<FilterDialog> filterDialogViewFactory;

    @Inject
    FilterDialogFactory(Provider<FilterDialog> filterDialogViewFactory) {
        this.filterDialogViewFactory = filterDialogViewFactory;
    }

    public CompletionStage<Optional<FilterFromDialog>> startCreateFilterDialog() {
        FilterDialog dialogView = filterDialogViewFactory.get();
        dialogView.setTitle(NEW_FILTER_DIALOG_TITLE);
        return FilterDialogPresenter.create(dialogView).show();
    }

    public CompletionStage<Optional<FilterFromDialog>> startCreateFilterDialogWithInitialData(
            FilterFromDialogData filter) {
        FilterDialog dialogView = filterDialogViewFactory.get();
        dialogView.setTitle(NEW_FILTER_DIALOG_TITLE);
        return FilterDialogPresenter.create(dialogView, filter).show();
    }

    public FilterDialogHandle startEditFilterDialog(FilterFromDialogData filter) {
        FilterDialog dialogView = filterDialogViewFactory.get();
        dialogView.setTitle(EDIT_FILTER_DIALOG_TITLE);
        FilterDialogPresenter dialogPresenter = FilterDialogPresenter.create(dialogView, filter);
        dialogPresenter.show();
        return dialogPresenter;
    }
}

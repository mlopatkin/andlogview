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

package org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.inject.Provider;

public class FilterDialogFactory {
    private final Provider<FilterDialog> filterDialogFactory;

    @Inject
    public FilterDialogFactory(Provider<FilterDialog> filterDialogFactory) {
        this.filterDialogFactory = filterDialogFactory;
    }

    public CompletionStage<Optional<FilterFromDialog>> startCreateFilterDialog() {
        return FilterDialogPresenter.create(filterDialogFactory.get()).show();
    }

    public CompletionStage<Optional<FilterFromDialog>> startEditFilterDialog(FilterFromDialog filter) {
        return FilterDialogPresenter.create(filterDialogFactory.get(), filter).show();
    }
}

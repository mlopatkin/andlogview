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

import name.mlopatkin.andlogview.filters.MutableFilterModel;
import name.mlopatkin.andlogview.ui.filterdialog.FilterDialogFactory;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filtertree.FilterNodeViewModel;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;

public class TreeNodeFilter extends BaseFilterPresenter implements FilterNodeViewModel {
    @AssistedInject
    public TreeNodeFilter(MutableFilterModel model, FilterDialogFactory dialogFactory,
            @Assisted FilterFromDialog filter) {
        super(model, dialogFactory, filter);
    }

    @Override
    public boolean isEnabled() {
        return filter.isEnabled();
    }

    @Override
    public String getText() {
        // TODO(mlopatkin): this is just a placeholder.
        return filter.getData().getTooltip();
    }

    @AssistedFactory
    interface Factory {
        TreeNodeFilter create(FilterFromDialog filter);
    }

    @Override
    public String toString() {
        return filter.toString();
    }
}

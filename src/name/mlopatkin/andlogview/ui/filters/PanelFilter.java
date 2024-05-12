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
import name.mlopatkin.andlogview.ui.filterpanel.FilterPanel;
import name.mlopatkin.andlogview.ui.filterpanel.PanelFilterView;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;

/**
 * A specialized {@link BaseFilterPresenter} that can be consumed by the {@link FilterPanel}.
 */
class PanelFilter extends BaseFilterPresenter implements PanelFilterView {
    @AssistedInject
    public PanelFilter(MutableFilterModel model, FilterDialogFactory dialogFactory, @Assisted FilterFromDialog filter) {
        super(model, dialogFactory, filter);
    }

    @Override
    public String getTooltip() {
        // TODO(mlopatkin) this should be a direct call
        return filter.getData().getTooltip();
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

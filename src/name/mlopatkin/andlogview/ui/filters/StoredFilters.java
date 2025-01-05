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

import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.Preference;
import name.mlopatkin.andlogview.config.SimpleClient;
import name.mlopatkin.andlogview.filters.FilterModel;
import name.mlopatkin.andlogview.filters.FiltersChangeObserver;
import name.mlopatkin.andlogview.filters.MutableFilterModel;
import name.mlopatkin.andlogview.utils.LazyInstance;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import javax.inject.Inject;

/**
 * This class manages saving the filter model.
 */
class StoredFilters {
    // TODO(mlopatkin) as the persistence schema evolves, it might make sense to decouple this class from the serialized
    //  forms and move it to :filters module.

    private final Preference<List<SavedFilterData>> preference;
    private final LazyInstance<MutableFilterModel> model;
    private final FiltersCodec codec = new FiltersCodec();

    /**
     * This constructor exists to ensure that {@link BufferFilterModel} can perform first step of filter model init.
     *
     * @param storage the storage
     * @param bufferFilterModel the buffer filter model that carries the configured model
     */
    @Inject
    StoredFilters(ConfigStorage storage, BufferFilterModel bufferFilterModel) {
        this(storage, bufferFilterModel.getConfiguredFilterModel());
    }

    @VisibleForTesting
    StoredFilters(ConfigStorage storage, MutableFilterModel model) {
        // Do not inline the variable, it trips NullAway over.
        TypeToken<List<SavedFilterData>> typeToken = new TypeToken<>() {};
        this.preference = storage.preference(new SimpleClient<>("filters", typeToken, ImmutableList::of));
        this.model = LazyInstance.lazy(() -> createModel(model));
    }

    public MutableFilterModel getStorageBackedModel() {
        return model.get();
    }

    private MutableFilterModel createModel(MutableFilterModel model) {
        codec.decode(preference.get()).forEach(model::addFilter);
        model.asObservable().addObserver(new FiltersChangeObserver(this::saveFilters));

        return model;
    }

    private void saveFilters(FilterModel model) {
        preference.set(codec.encode(model.getFilters()));
    }
}

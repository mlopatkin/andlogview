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

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.Preference;
import name.mlopatkin.andlogview.filters.Filter;
import name.mlopatkin.andlogview.filters.FilterModel;
import name.mlopatkin.andlogview.filters.MutableFilterModel;
import name.mlopatkin.andlogview.search.RequestCompilationException;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialogData;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.utils.LazyInstance;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * This class manages saving the filter model.
 */
@MainFrameScoped
public class StoredFilters {
    // TODO(mlopatkin) as the persistence schema evolves, it might make sense to decouple this class from the serialized
    //  forms and move it to :filters module.
    private static final Logger logger = Logger.getLogger(StoredFilters.class);

    private final Preference<List<SavedFilterData>> preference;
    private final LazyInstance<MutableFilterModel> model = LazyInstance.lazy(this::createModel);

    @Inject
    public StoredFilters(ConfigStorage storage) {
        this.preference = storage.preference(new FilterListSerializer());
    }

    public MutableFilterModel getStorageBackedModel() {
        return model.get();
    }

    private MutableFilterModel createModel() {
        var model = MutableFilterModel.create(
                preference.get().stream()
                        .map(this::decode)
                        .filter(Objects::nonNull)
                        .collect(toImmutableSet())
        );

        model.asObservable().addObserver(new FilterModel.Observer() {
            @Override
            public void onFilterAdded(FilterModel model, Filter newFilter) {
                saveFilters(model);
            }

            @Override
            public void onFilterRemoved(FilterModel model, Filter removedFilter) {
                saveFilters(model);
            }

            @Override
            public void onFilterReplaced(FilterModel model, Filter oldFilter, Filter newFilter) {
                saveFilters(model);
            }
        });

        return model;
    }

    private @Nullable Filter decode(SavedFilterData serializedForm) {
        try {
            return serializedForm.fromSerializedForm();
        } catch (RequestCompilationException e) {
            // Skip filters we've failed to load.
            logger.error("Failed to load filter", e);
            return null;
        }
    }

    private void saveFilters(FilterModel model) {
        preference.set(model.getFilters().stream()
                .filter(FilterFromDialogData.class::isInstance)
                .map(FilterFromDialogData.class::cast)
                .map(SavedDialogFilterData::new)
                .collect(Collectors.toList()));
    }
}

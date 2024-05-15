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

import name.mlopatkin.andlogview.config.PolymorphicTypeAdapterFactory;

import com.google.gson.TypeAdapterFactory;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

@Module
public class FilterGlobals {
    private static final String MIGRATED_TYPE_ID = "filters.SavedDialogFilterData";
    private static final String LEGACY_TYPE_ID =
            "org.bitbucket.mlopatkin.android.logviewer.filters.MainFilterController$SavedDialogFilterData";

    @Provides
    @IntoSet
    static TypeAdapterFactory typeAdapterFactory() {
        return new PolymorphicTypeAdapterFactory<>("classname", SavedFilterData.class)
                .subtype(SavedDialogFilterData.class, MIGRATED_TYPE_ID)
                .subtype(SavedDialogFilterData.class, LEGACY_TYPE_ID);
    }
}

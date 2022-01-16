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

import name.mlopatkin.andlogview.config.InvalidJsonContentException;
import name.mlopatkin.andlogview.config.NamedClient;
import name.mlopatkin.andlogview.filters.MainFilterController.SavedDialogFilterData;
import name.mlopatkin.andlogview.filters.MainFilterController.SavedFilterData;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class FilterListSerializer extends NamedClient<List<SavedFilterData>> {
    private enum ClassNameToken {
        LEGACY_DIALOG_FILTER_DATA(
                SavedDialogFilterData.class,
                "org.bitbucket.mlopatkin.android.logviewer.filters.MainFilterController$SavedDialogFilterData");

        public final Class<SavedFilterData> dataClass;
        public final String token;

        // Safe suppress because class is covariant here
        @SuppressWarnings("unchecked")
        ClassNameToken(Class<? extends SavedFilterData> dataClass, String token) {
            this.dataClass = (Class<SavedFilterData>) dataClass;
            this.token = token;
        }

        static @Nullable ClassNameToken getByToken(String classNameToken) {
            for (ClassNameToken value : values()) {
                if (value.token.equalsIgnoreCase(classNameToken)) {
                    return value;
                }
            }
            return null;
        }

        static @Nullable ClassNameToken getByClass(Class<? extends SavedFilterData> clazz) {
            for (ClassNameToken value : values()) {
                if (value.dataClass.equals(clazz)) {
                    return value;
                }
            }
            return null;
        }
    }

    public FilterListSerializer() {
        super("filters");
    }

    @Override
    public List<SavedFilterData> fromJson(Gson gson, JsonElement element) throws InvalidJsonContentException {
        JsonArray array = element.getAsJsonArray();
        ArrayList<SavedFilterData> result = new ArrayList<>(array.size());
        for (JsonElement filterData : array) {
            String filterClassName = filterData.getAsJsonObject().get("classname").getAsString();
            ClassNameToken classNameToken = ClassNameToken.getByToken(filterClassName);
            if (classNameToken == null) {
                throw new InvalidJsonContentException("Can't find class " + filterClassName + " to deserialize filter");
            }
            try {
                Class<SavedFilterData> filterClass = classNameToken.dataClass;
                result.add(gson.fromJson(filterData, filterClass));
            } catch (ClassCastException e) {
                throw new InvalidJsonContentException(
                        "Class '" + filterClassName + "' isn't a subclass of BaseToggleFilter");
            }
        }
        return result;
    }

    @Override
    public List<SavedFilterData> getDefault() {
        return Collections.emptyList();
    }

    @Override
    public JsonElement toJson(Gson gson, List<SavedFilterData> filters) {
        JsonArray array = new JsonArray();
        for (SavedFilterData filter : filters) {
            JsonElement filterElement = gson.toJsonTree(filter);
            ClassNameToken classNameToken = ClassNameToken.getByClass(filter.getClass());
            Objects.requireNonNull(classNameToken, "Can't find token for class " + filter.getClass());
            filterElement.getAsJsonObject().add("classname", new JsonPrimitive(classNameToken.token));
            array.add(filterElement);
        }
        return array;
    }
}

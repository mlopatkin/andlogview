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

package org.bitbucket.mlopatkin.android.logviewer.filters;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import org.bitbucket.mlopatkin.android.logviewer.filters.FilterStorage.InvalidJsonContentException;
import org.bitbucket.mlopatkin.android.logviewer.filters.MainFilterController.SavedFilterData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class FilterListSerializer
        implements FilterStorage.FilterStorageClient<List<SavedFilterData>> {

    public FilterListSerializer() {
    }

    @Override
    public String getName() {
        return "filters";
    }

    @Override
    public List<SavedFilterData> fromJson(Gson gson, JsonElement element) throws InvalidJsonContentException {
        JsonArray array = element.getAsJsonArray();
        ArrayList<SavedFilterData> result = new ArrayList<>(array.size());
        for (JsonElement filterData : array) {
            String filterClassName = filterData.getAsJsonObject().get("classname").getAsString();
            try {
                Class<?> filterClass = Class.forName(filterClassName);
                result.add(gson.fromJson(filterData, filterClass.asSubclass(SavedFilterData.class)));
            } catch (ClassNotFoundException e) {
                throw new InvalidJsonContentException("Can't find class '" + filterClassName + "' referenced in filter",
                                                      e);
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
            filterElement.getAsJsonObject().add("classname", new JsonPrimitive(filter.getClass().getName()));
            array.add(filterElement);
        }
        return array;
    }
}

/*
 * Copyright 2014 Mikhail Lopatkin
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

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.bitbucket.mlopatkin.utils.JsonUtils;
import org.bitbucket.mlopatkin.utils.jsonp.Json;
import org.bitbucket.mlopatkin.utils.jsonp.JsonBinder;
import org.bitbucket.mlopatkin.utils.jsonp.JsonWritable;

public class FilterJsonSerialization implements JsonWritable {

    private static final JsonBinder<Filter> filterBinder = Json
            .bindWithForClassNameFactory("filter_class", "filter_data", Filter.class);
    private final Filter filter;
    private final List<Class<?>> bindedClasses;

    public FilterJsonSerialization(Filter filter, List<Class<?>> bindedClasses) {
        this.filter = filter;
        this.bindedClasses = ImmutableList.copyOf(bindedClasses);
    }

    public FilterJsonSerialization(JSONObject json) throws JSONException {
        filter = filterBinder.get(json);
        ImmutableList.Builder<Class<?>> builder = ImmutableList.builder();
        for (String className : JsonUtils.asStringIterable(
                json.getJSONArray("filter_classes"))) {
            try {
                builder.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                throw new JSONException("Invalid filter_class " + className);
            }
        }
        bindedClasses = builder.build();
    }


    public void toJson(JSONObject obj) throws JSONException {
        filterBinder.put(filter, obj);
        List<String> list = Lists.newArrayListWithCapacity(bindedClasses.size());
        for (Class<?> clazz : bindedClasses) {
            list.add(clazz.getName());
        }
        obj.put("filter_classes", new JSONArray(list));
    }

    public Filter getFilter() {
        return filter;
    }

    public List<Class<?>> getBindedClasses() {
        return bindedClasses;
    }
}

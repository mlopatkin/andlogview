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

package org.bitbucket.mlopatkin.utils;

import java.util.Iterator;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.FluentIterable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class JsonUtils {

    private JsonUtils() {
    }

    private static abstract class JsonArrayIterable<T> extends FluentIterable<T> {

        private final JSONArray array;

        JsonArrayIterable(JSONArray array) {
            this.array = Preconditions.checkNotNull(array);
        }

        @Override
        public Iterator<T> iterator() {
            return new AbstractIterator<T>() {
                private final int length = array.length();
                private int position = 0;

                @Override
                protected T computeNext() {
                    if (position < length) {
                        try {
                            return JsonArrayIterable.this.get(array, position++);
                        } catch (JSONException e) {
                            throw Throwables.propagate(e);
                        }
                    }
                    return endOfData();
                }
            };
        }

        protected abstract T get(JSONArray array, int position) throws JSONException;

    }

    public static FluentIterable<String> asStringIterable(JSONArray array) {
        return new JsonArrayIterable<String>(array) {
            @Override
            protected String get(JSONArray array, int position) throws JSONException {
                return array.getString(position);
            }
        };
    }

    public static FluentIterable<Integer> asIntIterable(JSONArray array) {
        return new JsonArrayIterable<Integer>(array) {
            @Override
            protected Integer get(JSONArray array, int position) throws JSONException {
                return array.getInt(position);
            }
        };
    }

    public static FluentIterable<JSONObject> asObjectIterable(JSONArray array) {
        return new JsonArrayIterable<JSONObject>(array) {
            @Override
            protected JSONObject get(JSONArray array, int position) throws JSONException {
                return array.getJSONObject(position);
            }
        };
    }
}

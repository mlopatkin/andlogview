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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.FluentIterable;
import org.json.JSONArray;
import org.json.JSONException;

public final class JsonUtils {

    private JsonUtils() {
    }

    private static abstract class JsonArrayIterator<T> extends AbstractIterator<T> {

        protected final JSONArray array;
        private final int length;
        private int position;

        JsonArrayIterator(JSONArray array) {
            this.array = array;
            this.length = array.length();
            this.position = 0;
        }

        protected abstract T get(int position) throws JSONException;

        @Override
        protected T computeNext() {
            if (position < length) {
                try {
                    return get(position++);
                } catch (JSONException e) {
                    throw Throwables.propagate(e);
                }
            }
            return endOfData();
        }
    }


    private static class JsonArrayIterable<T> extends FluentIterable<T> {

        private final JSONArray array;
        private final Function<JSONArray, Iterator<T>> factory;

        JsonArrayIterable(JSONArray array, Function<JSONArray, Iterator<T>> iteratorFactory) {
            this.factory = Preconditions.checkNotNull(iteratorFactory);
            this.array = Preconditions.checkNotNull(array);
        }

        @Override
        public Iterator<T> iterator() {
            return factory.apply(array);
        }

        static <T1> JsonArrayIterable<T1> forArray(JSONArray array,
                Function<JSONArray, Iterator<T1>> iteratorFactory) {
            return new JsonArrayIterable<T1>(array, iteratorFactory);
        }
    }

    private static final Function<JSONArray, Iterator<String>> STRING_ITERATOR_MAKER
            = new Function<JSONArray, Iterator<String>>() {
        @Override
        public Iterator<String> apply(JSONArray jsonArray) {
            return new JsonArrayIterator<String>(jsonArray) {
                @Override
                protected String get(int position) throws JSONException {
                    return array.getString(position);
                }
            };
        }
    };

    private static final Function<JSONArray, Iterator<Integer>> INT_ITERATOR_MAKER
            = new Function<JSONArray, Iterator<Integer>>() {
        @Override
        public Iterator<Integer> apply(JSONArray jsonArray) {
            return new JsonArrayIterator<Integer>(jsonArray) {
                @Override
                protected Integer get(int position) throws JSONException {
                    return array.getInt(position);
                }
            };
        }
    };

    public static FluentIterable<String> asStringIterable(JSONArray array) {
        return JsonArrayIterable.forArray(array, STRING_ITERATOR_MAKER);
    }

    public static FluentIterable<Integer> asIntIterable(JSONArray array) {
        return JsonArrayIterable.forArray(array, INT_ITERATOR_MAKER);
    }
}

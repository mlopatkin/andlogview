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

package org.bitbucket.mlopatkin.utils.jsonp;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.bitbucket.mlopatkin.utils.JsonUtils;

@ParametersAreNonnullByDefault
public final class Json {

    private Json() {
    }

    public static JsonBinder<List<String>> bindStringList(final String fieldName) {
        return new JsonBinder<List<String>>() {
            @Override
            public List<String> get(JSONObject obj) throws JSONException {
                return Lists.newArrayList(JsonUtils.asStringIterable(obj.getJSONArray(fieldName)));
            }

            @Override
            public JSONObject put(List<String> value, JSONObject object) throws JSONException {
                return object.put(fieldName, new JSONArray(value));
            }
        };
    }

    public static <T extends JsonWritable> JsonBinder<T> bindWithForClassNameFactory(
            String classFieldName,
            String dataFieldName, Class<T> lowerBoundClass) {
        return new ForClassNameFactory<T>(classFieldName, dataFieldName, lowerBoundClass);
    }

    public static <T extends JsonWritable> JsonBinder<T> bindClass(Class<T> clazz) {
        final Constructor<T> constructor = getConstructorFromJson(clazz);

        return new JsonBinder<T>() {
            @Override
            public T get(JSONObject obj) throws JSONException {
                return newInstance(constructor, obj);

            }

            @Override
            public JSONObject put(T value, JSONObject object) throws JSONException {
                value.toJson(object);
                return object;
            }
        };
    }

    public static <T extends JsonWritable> JsonBinder<List<T>> bindList(final String fieldName,
            Class<T> elementClass) {

        final Constructor<T> elementConstructor = getConstructorFromJson(elementClass);

        return new JsonBinder<List<T>>() {
            @Override
            public List<T> get(JSONObject obj) throws JSONException {
                JSONArray list = obj.getJSONArray(fieldName);
                List<T> result = Lists.newArrayListWithCapacity(list.length());
                for (JSONObject element : JsonUtils.asObjectIterable(list)) {
                    result.add(newInstance(elementConstructor, element));
                }
                return result;
            }

            @Override
            public JSONObject put(List<T> value, JSONObject object) throws JSONException {
                JSONArray array = new JSONArray();
                for (T val : value) {
                    JSONObject obj = new JSONObject();
                    val.toJson(obj);
                    array.put(obj);
                }
                object.put(fieldName, array);
                return object;
            }
        };
    }

    private static <T> Constructor<T> getConstructorFromJson(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getConstructor(JSONObject.class);
            validateConstructor(constructor);
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Class " + clazz + "doesn't have " + clazz.getName() + "(JSONObject object)",
                    e);
        }
    }

    private static <T> T newInstance(Constructor<T> constructor, JSONObject object)
            throws JSONException {
        try {
            return constructor.newInstance(object);
        } catch (InstantiationException e) {
            throw Throwables.propagate(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            Throwables.propagateIfPossible(e.getCause(), JSONException.class);
            // we've got some weird checked exception
            throw new AssertionError(e);
        }
    }

    private static void validateConstructor(Constructor<?> constructor) {
        Class<?>[] possibleExceptionsArray = {JSONException.class};
        Set<Class<?>> possibleExceptions = Sets.newHashSet(possibleExceptionsArray);

        Set<Class<?>> actualExceptions = Sets.newHashSet();
        for (Class<?> exception : constructor.getExceptionTypes()) {
            if (isCheckedException(exception)) {
                actualExceptions.add(exception);
            }
        }

        Predicates.assignableFrom(JSONException.class);
        Preconditions.checkArgument(
                actualExceptions.isEmpty() || possibleExceptions.equals(actualExceptions),
                "Invalid exception signature, only org.json.JSONException is allowed");
    }

    private static boolean isCheckedException(Class<?> clazz) {
        return Throwable.class.isAssignableFrom(clazz) &&
                !Error.class.isAssignableFrom(clazz) &&
                !RuntimeException.class.isAssignableFrom(clazz);
    }
}

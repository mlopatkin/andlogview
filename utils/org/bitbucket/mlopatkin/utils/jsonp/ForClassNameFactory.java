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

import javax.annotation.Nonnull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that automagically serializes and deserializes objects to JSON based on runtime class.
 * <p>
 * Basically, class com.foo.Bar gets serialized into
 * <pre>
 * {
 *     'class_name': 'com.foo.Bar',
 *     'data': {
 *         ...
 *     }
 * }
 * </pre>
 * by using {@code ForClassNameFactory("class_name", "data", Bar.class)}.
 */
class ForClassNameFactory<T extends JsonWritable> implements JsonBinder<T> {

    private final String classNameField;
    private final String dataField;
    private final Class<T> lowerBoundClazz;

    public ForClassNameFactory(@Nonnull String classNameField, @Nonnull String dataField,
            @Nonnull Class<T> lowerBoundClazz) {
        this.classNameField = classNameField;
        this.dataField = dataField;
        this.lowerBoundClazz = lowerBoundClazz;
    }

    public T get(JSONObject object) throws JSONException {
        String className = object.getString(classNameField);
        try {
            Class<?> rawClazz = Class.forName(className);
            if (!lowerBoundClazz.isAssignableFrom(rawClazz)) {
                throw new JSONException(
                        className + " isn't a valid subclass of " + lowerBoundClazz);
            }

            Class<? extends T> castedClazz = rawClazz.asSubclass(lowerBoundClazz);
            return Json.bindClass(castedClazz).get(object.getJSONObject(dataField));
        } catch (ClassNotFoundException e) {
            throw new JSONException("Invalid class name specified: " + className);
        }
    }

    @Override
    public JSONObject put(T value, JSONObject object) throws JSONException {
        object.put(classNameField, value.getClass().getName());
        JSONObject dataObject = new JSONObject();
        value.toJson(dataObject);
        object.put(dataField, dataObject);
        return object;
    }


}

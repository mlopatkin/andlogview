/*
 * Copyright 2011 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.utils.properties;

public class PropertyBuilder<T> implements IPropertyBuilder<T> {

    private T defaultValue;
    private final Class<T> type;
    private Parser<T> parser;

    protected PropertyBuilder(Class<T> type) {
        this.type = type;
    }

    public PropertyBuilder<T> defaultVal(T value) {
        defaultValue = value;
        return this;
    }

    public PropertyBuilder<T> parser(Parser<T> p) {
        parser = p;
        return this;
    }

    public Property<T> build() {
        Property<T> result = new Property<T>(type, parser);
        result.setValue(defaultValue);
        return result;
    }

    static <T1> PropertyBuilder<T1> newPropertyBuilder(Class<T1> type) {
        return new PropertyBuilder<T1>(type);
    }
}

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

import java.util.EnumMap;

@SuppressWarnings("rawtypes")
public class EnumMapPropertyBuilder<T extends Enum<T>, V> implements IPropertyBuilder<EnumMap> {
    private final Class<T> keyType;
    private final Class<V> valueType;
    private Parser<V> parser;

    private EnumMapPropertyBuilder(Class<T> keyType, Class<V> valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public Property<EnumMap> build() {
        return new EnumMapProperty<>(keyType, valueType, parser);
    }

    public EnumMapPropertyBuilder<T, V> parser(Parser<V> parser) {
        this.parser = parser;
        return this;
    }

    static <T1 extends Enum<T1>, V1> EnumMapPropertyBuilder<T1, V1> newEnumMapPropertyBuilder(
            Class<T1> keyType, Class<V1> valueType) {
        return new EnumMapPropertyBuilder<>(keyType, valueType);
    }
}

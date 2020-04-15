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
import java.util.Properties;

@SuppressWarnings({"rawtypes", "NullAway"})
class EnumMapProperty<K extends Enum<K>, V> extends Property<EnumMap> {
    private final Parser<V> valueParser;
    private final Class<K> keyType;
    private final Class<V> valueType;

    EnumMapProperty(Class<K> keyType, Class<V> valueType, Parser<V> parser) {
        super(EnumMap.class, null);
        valueParser = parser;
        this.valueType = valueType;
        this.keyType = keyType;
        super.setValue(new EnumMap<K, V>(keyType));
    }

    @SuppressWarnings("unchecked")
    private EnumMap<K, V> getMap() {
        return super.getValue();
    }

    V getValue(K key) {
        return getMap().get(key);
    }

    void setValue(K key, V value) {
        if (!valueType.isInstance(value)) {
            throw new ClassCastException("Cannot cast value into value type");
        }
        getMap().put(key, value);
    }

    void parse(K key, String s) {
        getMap().put(key, valueParser.read(s));
    }

    @Override
    void parse(String s) {
        throw new UnsupportedOperationException("Simple parse is not available for EnumMap");
    }

    @Override
    EnumMap getValue() {
        throw new UnsupportedOperationException("Internal map cannot be retrieved. Use getValue(T key) instead.");
    }

    @Override
    void setValue(EnumMap map) {
        throw new UnsupportedOperationException("Internal map cannot be retrieved. Use setValue(T key) instead.");
    }

    @Override
    void assign(String key, Properties props) {
        for (K enumElem : keyType.getEnumConstants()) {
            String concreteKey = key + "." + enumElem.name();
            String value = props.getProperty(concreteKey);
            if (value != null) {
                setValue(enumElem, valueParser.read(value));
            }
        }
    }

    @Override
    public void write(String key, Properties outputProperties) {
        for (K enumElem : keyType.getEnumConstants()) {
            String concreteKey = key + "." + enumElem.name();
            V value = getMap().get(enumElem);
            if (value != null) {
                outputProperties.setProperty(concreteKey, valueParser.write(value));
            }
        }
    }
}

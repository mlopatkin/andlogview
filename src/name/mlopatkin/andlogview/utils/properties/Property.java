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

package name.mlopatkin.andlogview.utils.properties;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Properties;

class Property<T> {
    private @Nullable T value;
    private final Class<T> type;
    private final Parser<T> parser;

    Property(Class<T> type, Parser<T> parser) {
        this.type = type;
        this.parser = parser;
    }

    @Nullable T getValue() {
        return value;
    }

    void setValue(@Nullable T value) {
        if (value != null && !type.isInstance(value)) {
            throw new ClassCastException("Incompatible property type");
        }

        this.value = value;
    }

    @Override
    public String toString() {
        if (getParser() == null || value == null) {
            return "property{" + value + "}:" + type.getName();
        } else {
            return "property{" + getParser().write(value) + "}:" + type.getName();
        }
    }

    void assign(String key, Properties props) {
        if (getParser() == null) {
            throw new IllegalArgumentException("Property " + key + " cannot be parsed");
        }
        String value = props.getProperty(key);
        if (value != null) {
            setValue(getParser().read(value));
        }
    }

    void parse(String s) {
        setValue(getParser().read(s));
    }

    Parser<T> getParser() {
        return parser;
    }

    public void write(String key, Properties outputProperties) {
        T value = getValue();
        if (value != null) {
            outputProperties.setProperty(key, getParser().write(value));
        }
    }

    /**
     * Read-only property cannot be loaded from a file, only assigned from
     * resources
     *
     * @return
     */
    public boolean isReadOnly() {
        return false;
    }
}


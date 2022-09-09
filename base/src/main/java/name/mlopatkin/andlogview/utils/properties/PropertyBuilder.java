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

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PropertyBuilder<T> implements IPropertyBuilder<T> {
    private @Nullable T defaultValue;
    private final Class<T> type;
    private @MonotonicNonNull Parser<T> parser;

    protected PropertyBuilder(Class<T> type) {
        this.type = type;
    }

    public PropertyBuilder<T> defaultVal(@Nullable T value) {
        defaultValue = value;
        return this;
    }

    public PropertyBuilder<T> parser(Parser<T> p) {
        parser = p;
        return this;
    }

    @Override
    public Property<T> build() {
        assert parser != null;
        Property<T> result = new Property<>(type, parser);
        result.setValue(defaultValue);
        return result;
    }

    static <T1> PropertyBuilder<T1> newPropertyBuilder(Class<T1> type) {
        return new PropertyBuilder<>(type);
    }
}

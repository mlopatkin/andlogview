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

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("rawtypes")
public class ListPropertyBuilder<T> implements IPropertyBuilder<List> {
    private Class<T> elemType;
    private @MonotonicNonNull Parser<T> parser;
    private List<T> defaultVal = Collections.emptyList();

    private ListPropertyBuilder(Class<T> type) {
        elemType = type;
    }

    public ListPropertyBuilder<T> parser(Parser<T> parser) {
        this.parser = parser;
        return this;
    }

    public ListPropertyBuilder<T> defaultVal(List<T> value) {
        this.defaultVal = new ArrayList<>(value);
        return this;
    }

    @SafeVarargs
    public final ListPropertyBuilder<T> defaultVal(T... value) {
        this.defaultVal = Arrays.asList(value);
        return this;
    }

    static <T> ListPropertyBuilder<T> newListPropertyBuilder(Class<T> type) {
        return new ListPropertyBuilder<>(type);
    }

    @Override
    public Property<List> build() {
        assert parser != null;
        ListProperty<T> p = new ListProperty<>(elemType, parser);
        p.setValue(defaultVal);
        return p;
    }
}

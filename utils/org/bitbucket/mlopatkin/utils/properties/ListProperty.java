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

import java.util.List;

@SuppressWarnings({ "unchecked", "rawtypes" })
class ListProperty<T> extends Property<List> {

    private final Class<T> elemType;

    ListProperty(Class<T> type, Parser<T> parser) {
        super(List.class, new ListParser(parser));
        elemType = type;
    }

    @Override
    List<T> getValue() {
        return super.getValue();
    }

    @Override
    void setValue(List values) {
        for (Object value : values) {
            if (value != null && !elemType.isInstance(value)) {
                throw new ClassCastException("Incompatible property type");
            }
        }
        super.setValue(values);
    }
}

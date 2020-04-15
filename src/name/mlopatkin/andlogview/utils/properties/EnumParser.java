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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class EnumParser<T extends Enum<T>> implements Parser<T> {
    private final Map<String, T> lookupMap;

    EnumParser(Class<T> type) {
        Map<String, T> lookup = new HashMap<>();
        for (T value : type.getEnumConstants()) {
            lookup.put(value.name(), value);
        }
        lookupMap = Collections.unmodifiableMap(lookup);
    }

    @Override
    public T read(String value) {
        T result = lookupMap.get(value);
        if (result == null) {
            throw new IllegalArgumentException("Invalid enum value " + value);
        }
        return result;
    }

    @Override
    public String write(T value) {
        return value.name();
    }
}

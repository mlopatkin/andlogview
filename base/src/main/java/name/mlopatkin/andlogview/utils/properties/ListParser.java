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

import com.google.common.base.Splitter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Parses comma-separated list of values. No value should contain comma.
 */
class ListParser<T> implements Parser<List<T>> {
    private final Parser<T> internal;

    ListParser(Parser<T> parser) {
        internal = parser;
    }

    @Override
    public List<T> read(String value) {
        List<T> result = new ArrayList<>();
        for (String s : Splitter.on(',').trimResults().split(value)) {
            result.add(internal.read(s));
        }
        return result;
    }

    @Override
    public String write(@SuppressWarnings("NullableProblems") List<T> values) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<T> iter = values.iterator(); iter.hasNext(); ) {
            builder.append(internal.write(iter.next()));
            if (iter.hasNext()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
}

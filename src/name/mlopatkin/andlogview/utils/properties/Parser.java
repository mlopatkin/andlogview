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

/**
 * Interface to convert property values to and from strings to store them in
 * file.
 *
 * @param <T> actual type of the property value.
 */
public interface Parser<T> {
    /**
     * Parses value string and converts it into property object
     *
     * @param value string to parse
     * @return property value constructed from the string
     */
    T read(String value);

    /**
     * Encodes value into a string
     *
     * @param value value of a property
     * @return value encoded into a string
     */
    String write(T value);
}

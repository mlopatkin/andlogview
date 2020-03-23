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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Point;

/**
 * Static methods to show property traits - type, immutability and so on. You
 * may use static import to simplify code.
 */
public class PropertyTraits {
    private PropertyTraits() {}

    /**
     * Represents property of specified type. You may configure additional
     * options with chaining.
     *
     * @param <T> type of the property
     * @param type class object for the property
     * @return object for chaining
     */
    public static <T> PropertyBuilder<T> type(Class<T> type) {
        return PropertyBuilder.newPropertyBuilder(type);
    }

    /**
     * Represents property of specified type. You may configure additional
     * options with chaining.
     *
     * @param <T> type of the property
     * @param type class object for the property
     * @param parser parser to convert value to and from string
     * @return object for chaining
     */
    public static <T> PropertyBuilder<T> type(Class<T> type, Parser<T> parser) {
        return PropertyBuilder.newPropertyBuilder(type).parser(parser);
    }

    /**
     * Integer property that can be read from/stored in text file.
     *
     * @return object for chaining
     */
    public static PropertyBuilder<Integer> integer() {
        return type(Integer.class, Parsers.integerParser);
    }

    /**
     * String property that can be read from/stored in text file.
     *
     * @return object for chaining
     */
    public static PropertyBuilder<String> string() {
        return type(String.class, Parsers.stringParser);
    }

    public static PropertyBuilder<Boolean> bool() {
        return type(Boolean.class, Parsers.booleanParser);
    }

    public static PropertyBuilder<@Nullable Point> point() {
        return type(Point.class, Parsers.pointParser);
    }

    public static PropertyBuilder<String> string(String defaultValue) {
        return string().defaultVal(defaultValue);
    }

    public static PropertyBuilder<Integer> integer(int defaultValue) {
        return integer().defaultVal(defaultValue);
    }

    public static <T> ListPropertyBuilder<T> list(Class<T> type) {
        return ListPropertyBuilder.newListPropertyBuilder(type);
    }

    public static <T> ListPropertyBuilder<T> list(Class<T> type, Parser<T> elemParser) {
        return ListPropertyBuilder.newListPropertyBuilder(type).parser(elemParser);
    }

    public static <T extends Enum<T>, V> EnumMapPropertyBuilder<T, V> enumMap(Class<T> keyType, Class<V> valueType) {
        return EnumMapPropertyBuilder.newEnumMapPropertyBuilder(keyType, valueType);
    }

    public static <T extends Enum<T>, V> EnumMapPropertyBuilder<T, V> enumMap(
            Class<T> keyType, Class<V> valueType, Parser<V> valueParser) {
        return EnumMapPropertyBuilder.newEnumMapPropertyBuilder(keyType, valueType).parser(valueParser);
    }
}

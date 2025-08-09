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

import org.jspecify.annotations.Nullable;

import java.awt.Point;

public class Parsers {
    private Parsers() {}

    public static final Parser<String> stringParser = new StringParser();
    public static final Parser<Integer> integerParser = new IntegerParser();
    public static final Parser<Boolean> booleanParser = new BooleanParser();
    @SuppressWarnings("NullAway") // I have no idea why it complains
    public static final Parser<@Nullable Point> pointParser = new PointParser();

    public static <T extends Enum<T>> Parser<T> enumParser(Class<T> clazz) {
        return new EnumParser<>(clazz);
    }
}

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
package org.bitbucket.mlopatkin.utils;

import org.apache.commons.lang3.SystemUtils;

public class MyStringUtils {
    private MyStringUtils() {
    }

    public static boolean containsIgnoreCase(String src, String pattern) {
        if (src == null) {
            return false;
        }
        if (pattern == null) {
            return true;
        }

        src = src.toLowerCase();
        pattern = pattern.toLowerCase();
        return src.contains(pattern);
    }

    public static final int NOT_FOUND = -1;

    public static int indexOfIgnoreCase(String src, String pattern) {
        if (src == null || pattern == null) {
            throw new NullPointerException("Null argument is not allowed, src=" + src
                    + ", pattern=" + pattern);
        }
        src = src.toLowerCase();
        pattern = pattern.toLowerCase();
        return src.indexOf(pattern);
    }

    public static int indexOfIgnoreCase(String src, String pattern, int offset) {
        if (src == null || pattern == null) {
            throw new NullPointerException("Null argument is not allowed, src=" + src
                    + ", pattern=" + pattern);
        }
        src = src.toLowerCase();
        pattern = pattern.toLowerCase();
        return src.indexOf(pattern, offset);
    }

    public static String joinPath(String... elems) {
        StringBuilder b = new StringBuilder();
        for (String elem : elems) {
            int l = b.length();
            if (l > 0 && !SystemUtils.FILE_SEPARATOR.equals(b.substring(l - 1))) {
                b.append(SystemUtils.FILE_SEPARATOR);
            }
            b.append(elem);
        }
        return b.toString();
    }
}

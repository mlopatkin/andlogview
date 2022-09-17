/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.utils;

/**
 * Text utilities.
 */
public class TextUtils {
    private TextUtils() {}

    /**
     * Returns a proper noun for the given amount, following the rules of English. For example, this can be used as
     * {@code String.format("Got %d %s", numRecords, plural(numRecords, "record", "records"))} to get "Got 1 record" or
     * "Got 5 records".
     *
     * @param amount the amount of stuff
     * @param singular the singular noun for stuff, e.g. "record"
     * @param plural the plural noun for stuff, e.g. "records"
     * @return the proper version of noun that corresponds to the {@code amount}.
     */
    public static String plural(int amount, String singular, String plural) {
        return (amount == 1) ? singular : plural;
    }
}

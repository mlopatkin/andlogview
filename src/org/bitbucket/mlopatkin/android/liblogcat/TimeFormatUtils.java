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
package org.bitbucket.mlopatkin.android.liblogcat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeFormatUtils {

    private TimeFormatUtils() {
    }

    private static final ThreadLocal<DateFormat> LOGCAT_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        }
    };

    public static Date getTimeFromString(String s) throws ParseException {
        return LOGCAT_DATE_FORMAT.get().parse(s);
    }

    public static String convertTimeToString(Date date) {
        return LOGCAT_DATE_FORMAT.get().format(date);
    }
}

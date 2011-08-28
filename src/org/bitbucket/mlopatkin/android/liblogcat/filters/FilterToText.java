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
package org.bitbucket.mlopatkin.android.liblogcat.filters;

import org.apache.commons.lang3.StringUtils;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;

/**
 * Translates filtering params into text strings. Main goal is to load
 * complex filters into NewFilterDialog.
 * 
 */
public class FilterToText {

    public static String getTags(LogRecordFilter filter) {
        return getTags(getData(filter));
    }

    public static String getPids(LogRecordFilter filter) {
        return getPids(getData(filter));
    }

    public static String getMessage(LogRecordFilter filter) {
        return getMessage(getData(filter));
    }

    public static LogRecord.Priority getPriority(LogRecordFilter filter) {
        return getPriority(getData(filter));
    }

    private static FilterData getData(LogRecordFilter filter) {
        if (!(filter instanceof AbstractFilter)) {
            return null;
        }
        return ((AbstractFilter) filter).dumpFilter();
    }

    private static String getTags(FilterData data) {
        if (data != null) {
            return StringUtils.join(data.tags, ", ");
        } else {
            return null;
        }
    }

    private static String getPids(FilterData data) {
        if (data != null) {
            return StringUtils.join(data.pids, ", ");
        } else {
            return null;
        }
    }

    private static String getMessage(FilterData data) {
        if (data != null) {
            return data.message;
        } else {
            return null;
        }
    }

    private static Priority getPriority(FilterData data) {
        if (data != null) {
            return data.priority;
        } else {
            return null;
        }
    }
}

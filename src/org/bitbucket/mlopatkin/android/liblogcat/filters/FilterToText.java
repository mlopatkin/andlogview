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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;

/**
 * Translates filtering params into text strings. Main goal is to load
 * complex filters into NewFilterDialog.
 */
public class FilterToText {

    private static final Joiner commaJoiner = Joiner.on(", ");

    public static String getTags(LogRecordFilter filter) {
        return getTags(getData(filter));
    }

    public static String getPids(LogRecordFilter filter) {
        return getPids(getData(filter));
    }

    public static String getMessage(LogRecordFilter filter) {
        return getMessage(getData(filter));
    }

    public static String getAppNames(LogRecordFilter filter) {
        return getAppNames(getData(filter));
    }

    public static String getAppNamesAndPids(LogRecordFilter filter) {
        return getAppNamesAndPids(getData(filter));
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
            return commaJoiner.join(data.tags);
        } else {
            return null;
        }
    }

    private static String getPids(FilterData data) {
        if (data != null) {
            return commaJoiner.join(data.pids);
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

    private static String getAppNames(FilterData data) {
        if (data != null) {
            return commaJoiner.join(data.appNames);
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

    private static String getAppNamesAndPids(FilterData data) {
        if (data != null) {
            List<Object> allItems = new ArrayList<Object>(
                    data.pids != null ? data.pids : Collections.emptyList());
            allItems.addAll(data.appNames != null ? data.appNames : Collections.emptyList());
            return commaJoiner.join(allItems);
        } else {
            return null;
        }
    }
}

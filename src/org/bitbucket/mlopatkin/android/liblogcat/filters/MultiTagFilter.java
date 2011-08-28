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

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

public class MultiTagFilter extends AbstractFilter implements LogRecordFilter {

    private String[] tags;

    public MultiTagFilter(String[] tags) {
        this.tags = tags;
    }

    @Override
    public boolean include(LogRecord record) {
        for (String tag : tags) {
            if (tag.equalsIgnoreCase(record.getTag())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Tag" + (tags.length > 1 ? "s" : "") + ": " + StringUtils.join(tags, ", ");
    }

    @Override
    protected void dumpFilter(FilterData data) {
        data.tags = Arrays.asList(tags);
    }
}

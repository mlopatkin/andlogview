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

import java.util.EnumSet;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Kind;

public class LogKindFilter implements LogRecordFilter {

    private EnumSet<Kind> kinds = EnumSet.noneOf(Kind.class);

    @Override
    public boolean include(LogRecord record) {
        return kinds.contains(record.getKind());
    }

    public void setKindEnabled(Kind kind, boolean enabled) {
        if (enabled) {
            kinds.add(kind);
        } else {
            kinds.remove(kind);
        }
    }
}

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

package name.mlopatkin.andlogview.parsers.logcat;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;
import name.mlopatkin.andlogview.parsers.ParserControl;

import com.google.common.collect.ImmutableList;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public class ListCollectingHandler extends CollectingHandler {
    private final List<LogRecord> records = new ArrayList<>();

    public ListCollectingHandler() {}

    public ListCollectingHandler(IntFunction<@Nullable String> appNameLookup) {
        super(appNameLookup);
    }

    public ListCollectingHandler(Buffer buffer) {
        super(buffer);
    }

    @Override
    protected ParserControl logRecord(LogRecord record) {
        records.add(record);
        return ParserControl.proceed();
    }

    public List<LogRecord> getCollectedRecords() {
        return ImmutableList.copyOf(records);
    }
}

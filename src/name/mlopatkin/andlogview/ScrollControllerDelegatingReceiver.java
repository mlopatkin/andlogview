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

package name.mlopatkin.andlogview;

import name.mlopatkin.andlogview.logmodel.LogRecord;

import java.util.List;

public class ScrollControllerDelegatingReceiver implements BatchRecordsReceiver<LogRecord> {

    private final AutoScrollController scrollController;
    private final BatchRecordsReceiver<LogRecord> delegate;

    public ScrollControllerDelegatingReceiver(
            AutoScrollController scrollController,
            BatchRecordsReceiver<LogRecord> delegate) {
        this.scrollController = scrollController;
        this.delegate = delegate;
    }

    @Override
    public void addRecord(LogRecord record) {
        scrollController.notifyBeforeInsert();
        delegate.addRecord(record);
    }

    @Override
    public void addRecords(List<LogRecord> records) {
        scrollController.notifyBeforeInsert();
        delegate.addRecords(records);

    }

    @Override
    public void setRecords(List<LogRecord> records) {
        delegate.setRecords(records);
    }
}

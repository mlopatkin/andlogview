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

import name.mlopatkin.andlogview.logmodel.BatchRecordsReceiver;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.utils.TextUtils;

import org.apache.log4j.Logger;

import java.util.List;

public class ScrollControllerDelegatingReceiver implements BatchRecordsReceiver<LogRecord> {
    private static final Logger logger = Logger.getLogger(BatchRecordsReceiver.class);

    private final AutoScrollController scrollController;
    private final BatchRecordsReceiver<LogRecord> delegate;

    public ScrollControllerDelegatingReceiver(
            AutoScrollController scrollController,
            BatchRecordsReceiver<LogRecord> delegate) {
        this.scrollController = scrollController;
        this.delegate = delegate;
    }

    @Override
    public void addRecords(List<LogRecord> records) {
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Got batch of " + records.size() + " " + TextUtils.plural(records.size(), "record", "records"));
        }
        scrollController.notifyBeforeInsert();
        delegate.addRecords(records);

    }

    @Override
    public void setRecords(List<LogRecord> records) {
        delegate.setRecords(records);
    }
}

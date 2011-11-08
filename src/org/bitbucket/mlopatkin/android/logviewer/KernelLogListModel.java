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
package org.bitbucket.mlopatkin.android.logviewer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import org.bitbucket.mlopatkin.android.liblogcat.KernelLogRecord;

/**
 *
 */
public class KernelLogListModel extends AbstractListModel implements
        BatchRecordsReceiver<KernelLogRecord> {

    private final List<KernelLogRecord> records = new ArrayList<KernelLogRecord>();

    @Override
    public int getSize() {
        return records.size();
    }

    @Override
    public Object getElementAt(int index) {
        return records.get(index);
    }

    @Override
    public void addRecord(KernelLogRecord record) {
        records.add(record);
        int index = records.size() - 1;
        fireIntervalAdded(this, index, index);
    }

    @Override
    public void addRecords(List<KernelLogRecord> records) {
        int oldSize = this.records.size();
        if (records.isEmpty()) {
            return;
        }
        this.records.addAll(records);

        fireIntervalAdded(this, oldSize, this.records.size() - 1);
    }

    @Override
    public void setRecords(List<KernelLogRecord> records) {
        this.records.clear();
        this.records.addAll(records);

        fireContentsChanged(this, 0, this.records.size());
    }

    public void clear() {
        int lastIndex = getSize() - 1;
        this.records.clear();
        if (lastIndex >= 0) {
            fireIntervalRemoved(this, 0, lastIndex);
        }
    }
}

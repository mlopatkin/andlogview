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

import org.bitbucket.mlopatkin.android.liblogcat.DataSource;

public class PinRecordsController {

    private PinRecordsTableModel model;
    private PinRecordsTableColumnModel columnsModel;

    private PinRecordsFrame frame;

    public PinRecordsController(LogRecordTableModel baseModel, DataSource source) {
        model = new PinRecordsTableModel(baseModel);
        columnsModel = new PinRecordsTableColumnModel(source.getPidToProcessConverter());

        frame = new PinRecordsFrame(model, columnsModel);
    }

    public void pinRecord(int index) {
        if (!frame.isVisible()) {
            frame.setVisible(true);
        }
        model.pinRecord(index);
    }

    public void unpinRecord(int index) {
        model.unpinRecord(index);
    }
}

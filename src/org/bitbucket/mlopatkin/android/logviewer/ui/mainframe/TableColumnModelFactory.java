/*
 * Copyright 2018 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.mainframe;

import org.bitbucket.mlopatkin.android.logviewer.MainFrame;
import org.bitbucket.mlopatkin.android.logviewer.PidToProcessMapper;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableColumnModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.UserColumnOrder;

import java.util.Collection;

import javax.inject.Inject;

/**
 * The factory to produce {@link LogRecordTableColumnModel} for {@link MainFrame}.
 */
@MainFrameScoped
public class TableColumnModelFactory {
    private final UserColumnOrder columnOrder;

    @Inject
    public TableColumnModelFactory(UserColumnOrder columnOrder) {
        this.columnOrder = columnOrder;
    }

    public LogRecordTableColumnModel create(PidToProcessMapper pidToProcessMapper,
            Collection<Column> availableColumns) {
        LogRecordTableColumnModel model =
                new LogRecordTableColumnModel(pidToProcessMapper, availableColumns, columnOrder);
        model.asColumnOrderChangeObservable().addObserver(columnOrder::setColumnBefore);
        return model;
    }
}

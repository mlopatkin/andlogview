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

package name.mlopatkin.andlogview.ui.mainframe;

import name.mlopatkin.andlogview.MainFrame;
import name.mlopatkin.andlogview.PidToProcessMapper;
import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.ColumnTogglesModel;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableColumnModel;

import java.util.Collection;

import javax.inject.Inject;

/**
 * The factory to produce {@link LogRecordTableColumnModel} for {@link MainFrame}.
 */
@MainFrameScoped
public class TableColumnModelFactory {
    private final UserColumnOrder columnOrder;
    private final ColumnPrefs columnPrefs;

    @Inject
    public TableColumnModelFactory(ColumnPrefs columnPrefs) {
        this.columnOrder = columnPrefs.getColumnOrder();
        this.columnPrefs = columnPrefs;
    }

    public LogRecordTableColumnModel create(
            PidToProcessMapper pidToProcessMapper, Collection<Column> availableColumns) {
        LogRecordTableColumnModel model =
                new LogRecordTableColumnModel(pidToProcessMapper, columnOrder, buildTogglesModel(availableColumns));
        model.asColumnOrderChangeObservable().addObserver(columnOrder::setColumnBefore);
        return model;
    }

    private ColumnTogglesModel buildTogglesModel(Collection<Column> availableColumns) {
        return new ColumnTogglesModel() {
            @Override
            public boolean isColumnAvailable(Column column) {
                return availableColumns.contains(column) && columnPrefs.isColumnAvailable(column);
            }

            @Override
            public boolean isColumnVisible(Column column) {
                return isColumnAvailable(column) && columnPrefs.isColumnVisible(column);
            }

            @Override
            public void setColumnVisibility(Column column, boolean isVisible) {
                columnPrefs.setColumnVisibility(column, isVisible);
            }
        };
    }
}

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

import java.util.Arrays;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

public class IndexTableColumnModel extends LogRecordTableColumnModel {

    public IndexTableColumnModel(PidToProcessMapper pidToProcessMapper) {
        super(Arrays.asList("row", "time", "pid", "priority", "tag", "message"), pidToProcessMapper);
        for (int i = 0; i < getColumnCount(); ++i) {
            TableColumn column = getColumn(i);
            column.setCellEditor(noneditableCellEditor);
        }
    }

    @Override
    protected void initColumnInfo() {
        super.initColumnInfo();
        addColumnInfo("row", new ColumnInfo(LogRecordTableModel.COLUMN_LINE, "line", 30, 50));
    }

    private TableCellEditor noneditableCellEditor = new DefaultCellEditor(new JTextField()) {
        public boolean isCellEditable(java.util.EventObject anEvent) {
            return false;
        };
    };
}

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.bitbucket.mlopatkin.android.logviewer.widgets.TableColumnBuilder;

public class LogRecordTableColumnModel extends DefaultTableColumnModel {

    private TableCellRenderer timeCellRenderer = new LogRecordTimeCellRenderer();
    private TableCellRenderer priorityCellRenderer = new LogRecordPriorityCellRenderer();
    private TableCellRenderer messageCellRenderer = new HighlightCellRenderer();
    private TableCellRenderer tagCellRenderer = messageCellRenderer;
    private TableCellRenderer pidCellRender;

    private Map<String, TableColumnBuilder> columnInfo = new HashMap<String, TableColumnBuilder>();

    protected void addColumnInfo(String name, TableColumnBuilder info) {
        columnInfo.put(name, info);
    }

    protected void initColumnInfo() {
        addColumnInfo("time", new TableColumnBuilder(LogRecordTableModel.COLUMN_TIME, "Time")
                .setWidth(150).setMaxWidth(150).setRenderer(timeCellRenderer));
        addColumnInfo("pid", new TableColumnBuilder(LogRecordTableModel.COLUMN_PID, "pid")
                .setWidth(30).setMaxWidth(50).setRenderer(pidCellRender));
        addColumnInfo("tid", new TableColumnBuilder(LogRecordTableModel.COLUMN_TID, "tid")
                .setWidth(30).setMaxWidth(50));
        addColumnInfo("priority", new TableColumnBuilder(LogRecordTableModel.COLUMN_PRIORITY)
                .setWidth(30).setMaxWidth(50).setRenderer(priorityCellRenderer));
        addColumnInfo("tag", new TableColumnBuilder(LogRecordTableModel.COLUMN_TAG, "Tag")
                .setWidth(120).setRenderer(tagCellRenderer));
        addColumnInfo("message", new TableColumnBuilder(LogRecordTableModel.COLUMN_MSG, "Message")
                .setWidth(1000).setRenderer(messageCellRenderer));
    }

    private void addColumnByName(String name) {
        TableColumnBuilder builder = columnInfo.get(name);
        if (builder == null) {
            throw new IllegalArgumentException(name + " is not a valid column");
        }
        builder.setEditor(readOnlyCellEditor);
        addColumn(builder.build());
    }

    public LogRecordTableColumnModel(PidToProcessMapper pidToProcessMapper) {
        pidCellRender = new ToolTippedPidCellRenderer(pidToProcessMapper);
        initColumnInfo();
        for (String columnName : columnInfo.keySet()) {
            addColumnByName(columnName);
        }
    }

    public LogRecordTableColumnModel(List<String> columnNames, PidToProcessMapper pidToProcessMapper) {
        pidCellRender = new ToolTippedPidCellRenderer(pidToProcessMapper);
        initColumnInfo();
        for (String columnName : columnNames) {
            addColumnByName(columnName);
        }
    }

    private TableCellEditor readOnlyCellEditor = TableCellHelper.createReadOnlyCellTextEditor();
}

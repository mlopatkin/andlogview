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
import javax.swing.table.TableColumn;

public class LogRecordTableColumnModel extends DefaultTableColumnModel {

    private TableCellRenderer timeCellRenderer = new LogRecordTimeCellRenderer();
    private TableCellRenderer priorityCellRenderer = new LogRecordPriorityCellRenderer();
    private TableCellRenderer messageCellRenderer = new HighlightCellRenderer();
    private TableCellRenderer tagCellRenderer = messageCellRenderer;
    private TableCellRenderer pidCellRender;

    static class ColumnInfo {
        int modelColumn;
        int minWidth;
        int maxWidth;
        String columnName;
        TableCellRenderer renderer;

        public ColumnInfo(int modelColumn, String columnName, int minWidth,
                TableCellRenderer renderer) {
            this.modelColumn = modelColumn;
            this.minWidth = minWidth;
            this.renderer = renderer;
            this.columnName = columnName;
        }

        public ColumnInfo(int modelColumn, String columnName, int minWidth, int maxWidth,
                TableCellRenderer renderer) {
            this.modelColumn = modelColumn;
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            this.renderer = renderer;
            this.columnName = columnName;
        }

        public ColumnInfo(int modelColumn, String columnName, int minWidth) {
            this.modelColumn = modelColumn;
            this.minWidth = minWidth;
            this.columnName = columnName;
        }

        public ColumnInfo(int modelColumn, String columnName, int minWidth, int maxWidth) {
            this.modelColumn = modelColumn;
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            this.columnName = columnName;
        }
    }

    private Map<String, ColumnInfo> columnInfo = new HashMap<String, ColumnInfo>();

    protected void addColumnInfo(String name, ColumnInfo info) {
        columnInfo.put(name, info);
    }

    protected void initColumnInfo() {
        addColumnInfo("time", new ColumnInfo(LogRecordTableModel.COLUMN_TIME, "Time", 150, 150,
                timeCellRenderer));
        addColumnInfo("pid", new ColumnInfo(LogRecordTableModel.COLUMN_PID, "pid", 30, 50,
                pidCellRender));
        addColumnInfo("tid", new ColumnInfo(LogRecordTableModel.COLUMN_TID, "tid", 30, 50));
        addColumnInfo("priority", new ColumnInfo(LogRecordTableModel.COLUMN_PRIORITY, "", 30, 50,
                priorityCellRenderer));
        addColumnInfo("tag", new ColumnInfo(LogRecordTableModel.COLUMN_TAG, "Tag", 120,
                tagCellRenderer));
        addColumnInfo("message", new ColumnInfo(LogRecordTableModel.COLUMN_MSG, "Message", 1000,
                messageCellRenderer));
    }

    private void addColumnByName(String name) {
        ColumnInfo info = columnInfo.get(name);
        if (info == null) {
            throw new IllegalArgumentException(name + " is not a valid column");
        }

        TableColumn column = new TableColumn(info.modelColumn, info.minWidth, info.renderer, null);
        if (info.maxWidth != 0) {
            column.setMaxWidth(info.maxWidth);
        }
        column.setHeaderValue(info.columnName);
        column.setCellEditor(readOnlyCellEditor);
        addColumn(column);
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

package org.bitbucket.mlopatkin.android.logviewer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class LogcatTableColumnModel extends DefaultTableColumnModel {

    private TableCellRenderer timeCellRenderer = new LogcatTimeCellRenderer();
    private TableCellRenderer priorityCellRenderer = new LogcatPriorityCellRenderer();
    private TableCellRenderer messageCellRenderer = new ToolTippedCellRenderer();

    private static class ColumnInfo {
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

    private void initColumnInfo() {
        columnInfo.put("time", new ColumnInfo(LogRecordsTableModel.COLUMN_TIME, "Time", 150, 150,
                timeCellRenderer));
        columnInfo.put("pid", new ColumnInfo(LogRecordsTableModel.COLUMN_PID, "pid", 30, 50));
        columnInfo.put("tid", new ColumnInfo(LogRecordsTableModel.COLUMN_TID, "tid", 30, 50));
        columnInfo.put("priority", new ColumnInfo(LogRecordsTableModel.COLUMN_PRIORITY, "", 30, 50,
                priorityCellRenderer));
        columnInfo.put("tag", new ColumnInfo(LogRecordsTableModel.COLUMN_TAG, "Tag", 120));
        columnInfo.put("message", new ColumnInfo(LogRecordsTableModel.COLUMN_MSG, "Message", 1000,
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
        addColumn(column);
    }

    public LogcatTableColumnModel() {
        initColumnInfo();
        for (String columnName : columnInfo.keySet()) {
            addColumnByName(columnName);
        }
    }

    public LogcatTableColumnModel(List<String> columnNames) {
        initColumnInfo();
        for (String columnName : columnNames) {
            addColumnByName(columnName);
        }
    }

}

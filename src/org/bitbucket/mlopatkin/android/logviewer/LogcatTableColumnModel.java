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
        TableCellRenderer renderer;

        public ColumnInfo(int modelColumn, int minWidth, TableCellRenderer renderer) {
            this.modelColumn = modelColumn;
            this.minWidth = minWidth;
            this.renderer = renderer;
        }

        public ColumnInfo(int modelColumn, int minWidth) {
            this.modelColumn = modelColumn;
            this.minWidth = minWidth;
        }
    }

    private Map<String, ColumnInfo> columnInfo = new HashMap<String, ColumnInfo>();

    private void initColumnInfo() {
        columnInfo.put("time",
                new ColumnInfo(LogRecordsTableModel.COLUMN_TIME, 120, timeCellRenderer));
        columnInfo.put("pid", new ColumnInfo(LogRecordsTableModel.COLUMN_PID, 30));
        columnInfo.put("tid", new ColumnInfo(LogRecordsTableModel.COLUMN_TID, 30));
        columnInfo.put("priority", new ColumnInfo(LogRecordsTableModel.COLUMN_PRIORITY, 30,
                priorityCellRenderer));
        columnInfo.put("tag", new ColumnInfo(LogRecordsTableModel.COLUMN_TAG, 120));
        columnInfo.put("message", new ColumnInfo(LogRecordsTableModel.COLUMN_MSG, 800,
                messageCellRenderer));
    }

    private void addColumnByName(String name) {
        ColumnInfo info = columnInfo.get(name);
        if (info == null) {
            throw new IllegalArgumentException(name + " is not a valid column");
        }

        TableColumn column = new TableColumn(info.modelColumn, info.minWidth, info.renderer, null);
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

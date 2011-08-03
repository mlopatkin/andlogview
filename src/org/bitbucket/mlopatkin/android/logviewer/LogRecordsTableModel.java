package org.bitbucket.mlopatkin.android.logviewer;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class LogRecordsTableModel extends AbstractTableModel {

    private List<LogRecord> records;
    
    private static final int COLUMNS_COUNT = 6;
    
    public LogRecordsTableModel(List<LogRecord> records) {
        this.records = records;
    }

    @Override
    public int getColumnCount() {
        return COLUMNS_COUNT;
    }

    @Override
    public int getRowCount() {
        return records.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LogRecord record = records.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return record.getTime();
        case 1:
            return record.getPid();
        case 2:
            return record.getTid();
        case 3:
            return record.getPriority().getLetter();
        case 4:
            return record.getTag();
        case 5:
            return record.getMessage();
        }
        return null;
    }

}

package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.Color;
import java.awt.Component;
import java.util.EnumMap;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;

public class PriorityColoredCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = -5160005091082094580L;
    
    private static final EnumMap<Priority, Color> COLOR_MAP = new EnumMap<Priority, Color>(Priority.class);
    static {
        COLOR_MAP.put(Priority.VERBOSE, Color.BLACK);
        COLOR_MAP.put(Priority.ERROR, Color.RED);
        COLOR_MAP.put(Priority.INFO, Color.GREEN);
        COLOR_MAP.put(Priority.WARN, Color.ORANGE);
        COLOR_MAP.put(Priority.DEBUG, Color.BLUE);
    }
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Priority priority = (Priority) table.getModel().getValueAt(row, LogRecordsTableModel.COLUMN_PRIORITY);
        result.setForeground(COLOR_MAP.get(priority));
        return result;
    }
}

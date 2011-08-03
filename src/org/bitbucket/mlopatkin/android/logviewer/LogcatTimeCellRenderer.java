package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.Component;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;

import org.bitbucket.mlopatkin.android.liblogcat.TimeFormatUtils;

public class LogcatTimeCellRenderer extends PriorityColoredCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (!(value instanceof Date)) {
            throw new IllegalArgumentException(
                    "Incorrect value class passed into LogcatTimeCellRenderer");
        }
        Date dateValue = (Date) value;
        JLabel result = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                row, column);
        result.setText(TimeFormatUtils.convertTimeToString(dateValue));
        return result;
    }
}

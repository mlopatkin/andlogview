package org.bitbucket.mlopatkin.android.logviewer;

import java.util.Date;

import org.bitbucket.mlopatkin.android.liblogcat.TimeFormatUtils;

public class LogcatTimeCellRenderer extends PriorityColoredCellRenderer {
    @Override
    protected void setValue(Object value) {
        if (!(value instanceof Date)) {
            throw new IllegalArgumentException(
                    "Incorrect value class passed into LogcatTimeCellRenderer");
        }
        Date dateValue = (Date) value;
        super.setValue(TimeFormatUtils.convertTimeToString(dateValue));
    }

}

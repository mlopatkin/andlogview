package org.bitbucket.mlopatkin.android.logviewer;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;

public class LogcatPriorityCellRenderer extends PriorityColoredCellRenderer {
    @Override
    protected void setValue(Object value) {
        if (!(value instanceof Priority)) {
            throw new IllegalArgumentException(
                    "Incorrect value class passed into LogcatPriorityCellRenderer");
        }

        super.setValue(value.toString().substring(0, 1));

    }
}

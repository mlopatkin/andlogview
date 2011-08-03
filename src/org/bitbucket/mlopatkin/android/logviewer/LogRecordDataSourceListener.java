package org.bitbucket.mlopatkin.android.logviewer;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

public interface LogRecordDataSourceListener {
    public void onNewRecord(LogRecord record);
}

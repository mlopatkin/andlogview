package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.Rectangle;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

class AutoScrollController implements LogRecordDataSourceListener, TableModelListener {
    private static final int THRESHOLD = Configuration.ui.autoscrollThreshold();

    private JTable table;
    private LogRecordsTableModel model;

    public AutoScrollController(JTable table, LogRecordsTableModel model) {
        this.table = table;
        this.model = model;
        this.model.addTableModelListener(this);
    }

    @Override
    public void onNewRecord(LogRecord record) {
        model.addRecord(record);
        shouldScroll = isAtBottom();
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (shouldScroll) {
            Rectangle bottomRect = table.getBounds();
            bottomRect.y = bottomRect.height - THRESHOLD;
            table.scrollRectToVisible(bottomRect);
        }
    }

    private boolean shouldScroll;

    private boolean isAtBottom() {
        int bottom = table.getBounds().height;
        int top = table.getVisibleRect().y;
        int height = table.getVisibleRect().height;
        int delta = Math.abs(bottom - (top + height));
        boolean atBottom = delta <= THRESHOLD;
        return atBottom;
    }

}

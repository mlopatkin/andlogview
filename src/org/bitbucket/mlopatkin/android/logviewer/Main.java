package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

public class Main implements LogRecordDataSourceListener {

    private JFrame frmAndroidLogViewer;
    private JTable logElements;
    private JScrollPane scrollPane;

    private LogRecordsTableModel recordsModel = new LogRecordsTableModel();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Main window = new Main();
                    window.frmAndroidLogViewer.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Create the application.
     */
    public Main() {
        initialize();
        final AdbDataSource source = new AdbDataSource(this);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                source.close();
            }
        });
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmAndroidLogViewer = new JFrame();
        frmAndroidLogViewer.setTitle("Android Log Viewer");
        frmAndroidLogViewer.setBounds(100, 100, 1000, 450);
        frmAndroidLogViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        logElements = new JTable();
        logElements.setFillsViewportHeight(true);
        logElements.setShowGrid(false);

        logElements.setModel(recordsModel);
        logElements.setDefaultRenderer(Object.class, new PriorityColoredCellRenderer());
        logElements.setColumnModel(new LogcatTableColumnModel(Configuration.ui.columns()));

        recordsModel.addTableModelListener(addRecordListener);

        scrollPane = new JScrollPane(logElements);
        frmAndroidLogViewer.getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private boolean isAtBottom() {
        int bottom = logElements.getBounds().height;
        int top = logElements.getVisibleRect().y;
        int height = logElements.getVisibleRect().height;
        boolean atBottom = Math.abs(bottom - (top + height)) <= Configuration.ui
                .autoscrollThreshold();
        return atBottom;
    }

    boolean shouldScroll;

    private TableModelListener addRecordListener = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
            if (shouldScroll) {
                Rectangle bottomRect = logElements.getBounds();
                bottomRect.y = bottomRect.height - Configuration.ui.autoscrollThreshold();
                logElements.scrollRectToVisible(bottomRect);
            }
        }
    };
    @Override
    public void onNewRecord(LogRecord record) {
        shouldScroll = isAtBottom();
        recordsModel.addRecord(record);
    }

}

package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class Main {

    private JFrame frmAndroidLogViewer;
    private JTable logElements;
    private JScrollPane scrollPane;

    private LogRecordsTableModel recordsModel = new LogRecordsTableModel();
    private AutoScrollController scrollController;

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
        scrollController = new AutoScrollController(logElements, recordsModel);
        final AdbDataSource source = new AdbDataSource(scrollController);

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

        scrollPane = new JScrollPane(logElements);
        frmAndroidLogViewer.getContentPane().add(scrollPane, BorderLayout.CENTER);
    }


}

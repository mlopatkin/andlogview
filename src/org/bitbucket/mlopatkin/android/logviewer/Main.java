package org.bitbucket.mlopatkin.android.logviewer;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTable;

import test.TestDataLoader;

public class Main {

    private JFrame frmAndroidLogViewer;
    private JTable logElements;

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
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmAndroidLogViewer = new JFrame();
        frmAndroidLogViewer.setTitle("Android Log Viewer");
        frmAndroidLogViewer.setBounds(100, 100, 450, 300);
        frmAndroidLogViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        logElements = new JTable();
        logElements.setShowGrid(false);
        frmAndroidLogViewer.getContentPane().add(logElements, BorderLayout.CENTER);
        LogRecordsTableModel recordsModel = new LogRecordsTableModel(TestDataLoader.getRecords());
        logElements.setModel(recordsModel);
        logElements.setDefaultRenderer(Object.class, new PriorityColoredCellRenderer());
        logElements.setColumnModel(new LogcatTableColumnModel(Configuration.ui.columns()));        
    }

}

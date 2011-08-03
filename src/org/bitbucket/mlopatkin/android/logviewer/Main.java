package org.bitbucket.mlopatkin.android.logviewer;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

import test.TestDataLoader;

public class Main {

    private JFrame frame;
    private JTable logElements;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Main window = new Main();
                    window.frame.setVisible(true);
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
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        logElements = new JTable();
        logElements.setShowGrid(false);
        frame.getContentPane().add(logElements, BorderLayout.CENTER);
        LogRecordsTableModel recordsModel = new LogRecordsTableModel(TestDataLoader.getRecords());
        logElements.setModel(recordsModel);
        logElements.setDefaultRenderer(Object.class, new PriorityColoredCellRenderer());
        logElements.setDefaultRenderer(Date.class, new LogcatTimeCellRenderer());
        logElements.setDefaultRenderer(LogRecord.Priority.class, new LogcatPriorityCellRenderer());
                
    }

}

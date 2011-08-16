/*
 * Copyright 2011 Mikhail Lopatkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

public class PinRecordsFrame extends JFrame {

    private static final Logger logger = Logger.getLogger(PinRecordsFrame.class);

    private JPanel contentPane;
    private DecoratingRendererTable pinnedRecordsTable;
    private PinRecordsController controller;

    public PinRecordsFrame(LogRecordTableModel model, PinRecordsTableColumnModel columnsModel,
            PinRecordsController controller) {
        initialize();
        pinnedRecordsTable.setModel(model);
        pinnedRecordsTable.setColumnModel(columnsModel);
        pinnedRecordsTable.setTransferHandler(new LogRecordsTransferHandler());
        this.controller = controller;
    }

    private void initialize() {
        setTitle("Pinned log records");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds(100, 100, 1000, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);

        pinnedRecordsTable = new DecoratingRendererTable();
        pinnedRecordsTable.addDecorator(new PriorityColoredCellRenderer());
        pinnedRecordsTable.setFillsViewportHeight(true);
        pinnedRecordsTable.setShowGrid(false);
        pinnedRecordsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pinnedRecordsTable.addMouseListener(new LineDoubleClickListener());
        scrollPane.setViewportView(pinnedRecordsTable);
    }

    JTable getTable() {
        return pinnedRecordsTable;
    }

    private class LineDoubleClickListener extends MouseAdapter implements MouseListener {

        private static final int DOUBLE_CLICK_COUNT = 2;

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == DOUBLE_CLICK_COUNT && e.getButton() == MouseEvent.BUTTON1) {
                int rowView = pinnedRecordsTable.rowAtPoint(e.getPoint());
                if (rowView >= 0) {
                    int row = pinnedRecordsTable.convertRowIndexToModel(rowView);
                    controller.activateRow(row);
                }
            }
        }

    }
}

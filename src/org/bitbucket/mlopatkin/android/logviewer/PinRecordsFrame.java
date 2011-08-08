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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

public class PinRecordsFrame extends JFrame {

    private JPanel contentPane;
    private DecoratingRendererTable pinnedRecordsTable;

    public PinRecordsFrame(PinRecordsTableModel model, PinRecordsTableColumnModel columnsModel) {
        initialize();
        pinnedRecordsTable.setModel(model);
        pinnedRecordsTable.setColumnModel(columnsModel);
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
        scrollPane.setViewportView(pinnedRecordsTable);
    }

    JTable getTable() {
        return pinnedRecordsTable;
    }
}

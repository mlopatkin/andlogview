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
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import org.bitbucket.mlopatkin.android.logviewer.widgets.DecoratingRendererTable;
import org.bitbucket.mlopatkin.android.logviewer.widgets.TablePopupMenu;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;
import org.bitbucket.mlopatkin.android.logviewer.widgets.TablePopupMenu.ItemsUpdater;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper.DoubleClickListener;

public class IndexFrame extends JFrame implements ItemsUpdater {

    private JPanel contentPane;
    private DecoratingRendererTable indexedRecordsTable;
    private IndexController controller;
    private TablePopupMenu popupMenu = new TablePopupMenu();
    private Action acCopy;

    public IndexFrame(LogRecordTableModel model, IndexTableColumnModel columnsModel,
            IndexController controller) {
        this.controller = controller;
        initialize();
        indexedRecordsTable.setModel(model);
        indexedRecordsTable.setColumnModel(columnsModel);
        indexedRecordsTable.setTransferHandler(new LogRecordsTransferHandler());
        acCopy = UiHelper.createActionWrapper(indexedRecordsTable, "copy", "Copy", "control C");
        popupMenu.add(acCopy);
        popupMenu.addItemsUpdater(this);
        addWindowListener(closingListener);
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds(100, 100, 1000, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);

        indexedRecordsTable = new DecoratingRendererTable();
        indexedRecordsTable.addDecorator(new PriorityColoredCellRenderer());
        indexedRecordsTable.setFillsViewportHeight(true);
        indexedRecordsTable.setShowGrid(false);
        UiHelper.addDoubleClickListener(indexedRecordsTable, new LineDoubleClickListener());
        UiHelper.addPopupMenu(indexedRecordsTable, popupMenu);
        scrollPane.setViewportView(indexedRecordsTable);

        setupKeys();
    }

    JTable getTable() {
        return indexedRecordsTable;
    }

    TablePopupMenu getPopupMenu() {
        return popupMenu;
    }

    private class LineDoubleClickListener implements DoubleClickListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            int rowView = indexedRecordsTable.rowAtPoint(e.getPoint());
            if (rowView >= 0) {
                int row = indexedRecordsTable.convertRowIndexToModel(rowView);
                controller.activateRow(row);
            }
        }

    }

    private WindowListener closingListener = new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            controller.onWindowClosed();
        };
    };

    private static final String KEY_JUMP_TO_LINE = "ENTER";
    private static final String ACTION_JUMP_TO_LINE = "jump_to_line";

    private void setupKeys() {
        UiHelper.bindKeyFocused(indexedRecordsTable, KEY_JUMP_TO_LINE, ACTION_JUMP_TO_LINE,
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int row = indexedRecordsTable.getSelectedRow();
                        if (row >= 0) {
                            row = indexedRecordsTable.convertRowIndexToModel(row);
                            controller.activateRow(row);
                        }
                    }
                });
    }

    @Override
    public void updateItemsState(JTable source) {
        acCopy.setEnabled(source.getSelectedRowCount() > 0);
    }
}

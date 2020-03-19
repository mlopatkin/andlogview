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
package org.bitbucket.mlopatkin.android.logviewer.ui.mainframe;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogTable;
import org.bitbucket.mlopatkin.android.logviewer.widgets.TablePopupMenu;
import org.bitbucket.mlopatkin.android.logviewer.widgets.TablePopupMenu.ItemsUpdater;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;

import java.awt.event.ActionEvent;

import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JTable;

public class LogRecordPopupMenuHandler implements ItemsUpdater {
    private final Action acAddToBookmarks = new AbstractAction("Add to bookmarks") {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int rowIndex : getSelectedRows()) {
                bookmarkModel.addRecord(getRecord(rowIndex));
            }
        }
    };

    private final Action acRemoveFromBookmarks = new AbstractAction("Remove from bookmarks") {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int rowIndex : getSelectedRows()) {
                bookmarkModel.removeRecord(getRecord(rowIndex));
            }
        }
    };

    private Action acCopy;

    private JTable table;
    private LogRecordTableModel model;
    private final BookmarkModel bookmarkModel;

    private JMenuItem itemAddToBookmarks = new JMenuItem(acAddToBookmarks);
    private JMenuItem itemRemoveBookmarks = new JMenuItem(acRemoveFromBookmarks);

    private LogRecordPopupMenuHandler(LogTable table, LogRecordTableModel model, BookmarkModel bookmarkModel) {
        this.table = table;
        this.model = model;
        this.bookmarkModel = bookmarkModel;
    }

    private void setUpMenu() {
        TablePopupMenu popupMenu = new TablePopupMenu();
        popupMenu.addItemsUpdater(this);
        acCopy = UiHelper.createActionWrapper(table, "copy", "Copy", "control C");
        popupMenu.add(acCopy);
        popupMenu.addSeparator();
        popupMenu.add(itemAddToBookmarks);
        popupMenu.add(itemRemoveBookmarks);
        UiHelper.addPopupMenu(table, popupMenu);
    }

    protected int[] getSelectedRows() {
        return table.getSelectedRows();
    }

    protected int getSelectedRow() {
        return table.getSelectedRow();
    }

    @Override
    public void updateItemsState(JTable source) {
        switch (source.getSelectedRowCount()) {
            case 0:
                acAddToBookmarks.setEnabled(false);
                acRemoveFromBookmarks.setEnabled(false);
                acCopy.setEnabled(false);
                toggleAddRemoveState(true);
                break;
            case 1:
                acAddToBookmarks.setEnabled(true);
                acRemoveFromBookmarks.setEnabled(true);
                acCopy.setEnabled(true);
                adjustAddRemoveVisibilty();
                break;
            default:
                acAddToBookmarks.setEnabled(false);
                acRemoveFromBookmarks.setEnabled(false);
                acCopy.setEnabled(true);
                toggleAddRemoveState(true);
        }
    }

    private void adjustAddRemoveVisibilty() {
        assert table.getSelectedRowCount() == 1;
        boolean marked = bookmarkModel.containsRecord(getRecord(getSelectedRow()));
        toggleAddRemoveState(!marked);
    }

    private void toggleAddRemoveState(boolean canAdd) {
        itemAddToBookmarks.setVisible(canAdd);
        itemRemoveBookmarks.setVisible(!canAdd);
    }

    private LogRecord getRecord(int tableRowIndex) {
        return model.getRowData(table.convertRowIndexToModel(tableRowIndex));
    }

    @MainFrameScoped
    public static class Factory {
        private final LogRecordTableModel tableModel;
        private final BookmarkModel bookmarkModel;

        @Inject
        public Factory(LogRecordTableModel tableModel, BookmarkModel bookmarkModel) {
            this.tableModel = tableModel;
            this.bookmarkModel = bookmarkModel;
        }

        public void attachMenuHandle(LogTable table) {
            LogRecordPopupMenuHandler handler = new LogRecordPopupMenuHandler(table, tableModel, bookmarkModel);
            handler.setUpMenu();
        }
    }
}

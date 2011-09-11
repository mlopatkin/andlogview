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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.filters.MultiPidFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.SingleTagFilter;
import org.bitbucket.mlopatkin.android.logviewer.TablePopupMenu.ItemsUpdater;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;

public class LogRecordPopupMenuHandler implements ItemsUpdater {

    private Action acHideByTag = new AbstractAction("Hide with this tag") {
        @Override
        public void actionPerformed(ActionEvent e) {
            filterController.addFilter(FilteringMode.HIDE, new SingleTagFilter(
                    getSelectedLogRecord().getTag()));
        }
    };

    private Action acHideByPid = new AbstractAction("Hide with this pid") {
        @Override
        public void actionPerformed(ActionEvent e) {
            filterController.addFilter(FilteringMode.HIDE, new MultiPidFilter(
                    new int[] { getSelectedLogRecord().getPid() }));
        }
    };

    private Action acAddToBookmarks = new AbstractAction("Add to bookmarks") {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int rowIndex : getSelectedRows())
                bookmarksController.markRecord(table.convertRowIndexToModel(rowIndex));
        }
    };

    private Action acRemoveFromBookmarks = new AbstractAction("Remove from bookmarks") {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int rowIndex : getSelectedRows())
                bookmarksController.unmarkRecord(table.convertRowIndexToModel(rowIndex));
        }
    };

    private Action acCopy;

    private BookmarksController bookmarksController;
    private FilterController filterController;
    private JTable table;
    private LogRecordTableModel model;

    private JMenuItem itemAddToBookmarks = new JMenuItem(acAddToBookmarks);
    private JMenuItem itemRemoveBookmarks = new JMenuItem(acRemoveFromBookmarks);

    private void setUpMenu() {
        JPopupMenu popupMenu = new TablePopupMenu(this);
        acCopy = UiHelper.createActionWrapper(table, "copy", "Copy", "control C");
        popupMenu.add(acCopy);
        popupMenu.addSeparator();
        popupMenu.add(acHideByTag);
        popupMenu.add(acHideByPid);
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

    protected LogRecord getSelectedLogRecord() {
        return model.getRowData(table.convertRowIndexToModel(getSelectedRow()));
    }

    public LogRecordPopupMenuHandler(JTable table, LogRecordTableModel model,
            final FilterController filterController, final BookmarksController bookmarksController) {
        this.table = table;
        this.model = model;
        this.filterController = filterController;
        this.bookmarksController = bookmarksController;
        setUpMenu();
    }

    @Override
    public void updateItemsState(JTable source) {
        switch (source.getSelectedRowCount()) {
        case 0:
            acHideByTag.setEnabled(false);
            acHideByPid.setEnabled(false);
            acAddToBookmarks.setEnabled(false);
            acRemoveFromBookmarks.setEnabled(false);
            acCopy.setEnabled(false);
            toggleAddRemoveState(true);
            break;
        case 1:
            acHideByTag.setEnabled(true);
            acHideByPid.setEnabled(true);
            acAddToBookmarks.setEnabled(true);
            acRemoveFromBookmarks.setEnabled(true);
            acCopy.setEnabled(true);
            adjustAddRemoveVisibilty();
            break;
        default:
            acHideByTag.setEnabled(false);
            acHideByPid.setEnabled(false);
            acAddToBookmarks.setEnabled(false);
            acRemoveFromBookmarks.setEnabled(false);
            acCopy.setEnabled(true);
            toggleAddRemoveState(true);
        }
    }

    private void adjustAddRemoveVisibilty() {
        assert table.getSelectedRowCount() == 1;
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        boolean marked = bookmarksController.isMarked(row);
        toggleAddRemoveState(!marked);
    }

    private void toggleAddRemoveState(boolean canAdd) {
        itemAddToBookmarks.setVisible(canAdd);
        itemRemoveBookmarks.setVisible(!canAdd);
    }
}

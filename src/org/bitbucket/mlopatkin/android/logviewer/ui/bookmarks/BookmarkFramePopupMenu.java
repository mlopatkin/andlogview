/*
 * Copyright 2015 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.bookmarks;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexframe.PopupBuilder;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableModel;
import org.bitbucket.mlopatkin.android.logviewer.widgets.TablePopupMenu;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.KeyStroke;

public class BookmarkFramePopupMenu implements TablePopupMenu.ItemsUpdater {

    private final LogRecordTableModel tableModel;
    private final BookmarkModel bookmarkModel;
    private final JTable table;

    private final Action acDeleteBookmarks = new AbstractAction("Remove from bookmarks") {
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            Set<LogRecord> recordsToRemove = new HashSet<>();
            for (int rowIndex : table.getSelectedRows()) {
                recordsToRemove.add(tableModel.getRowData(table.convertRowIndexToModel(rowIndex)));
            }
            recordsToRemove.forEach(bookmarkModel::removeRecord);
        }
    };

    private BookmarkFramePopupMenu(LogRecordTableModel tableModel,
                                   BookmarkModel bookmarkModel,
                                   JTable table) {
        this.tableModel = tableModel;
        this.bookmarkModel = bookmarkModel;
        this.table = table;
    }

    private void appendItemsTo(TablePopupMenu menu) {
        menu.addItemsUpdater(this);
        menu.add(acDeleteBookmarks);
        UiHelper.bindKeyFocused(table, "DELETE", "remove_bookmark", acDeleteBookmarks);
        updateItemsState(table);
    }

    @Override
    public void updateItemsState(JTable source) {
        acDeleteBookmarks.setEnabled(source.getSelectedRowCount() > 0);
    }

    public static class Factory implements PopupBuilder {
        private final LogRecordTableModel model;
        private final BookmarkModel bookmarkModel;

        @Inject
        Factory(LogRecordTableModel model, BookmarkModel bookmarkModel) {
            this.model = model;
            this.bookmarkModel = bookmarkModel;
        }

        @Override
        public TablePopupMenu appendPopupMenuItems(JTable table, TablePopupMenu menu) {
            BookmarkFramePopupMenu bookmarkMenu = new BookmarkFramePopupMenu(model, bookmarkModel, table);
            bookmarkMenu.appendItemsTo(menu);
            return menu;
        }
    }
}

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
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.filters.MultiPidFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.SingleTagFilter;

public class LogRecordPopupMenuHandler extends TablePopupMenuHandler {

    private Action acHideByTag = new AbstractAction("Hide with this tag") {
        @Override
        public void actionPerformed(ActionEvent e) {
            filterController.addFilter(FilteringMode.HIDE, new SingleTagFilter(
                    getLogRecordAtPoint().getTag()));
        }
    };

    private Action acHideByPid = new AbstractAction("Hide with this pid") {
        @Override
        public void actionPerformed(ActionEvent e) {
            filterController.addFilter(FilteringMode.HIDE, new MultiPidFilter(
                    new int[] { getLogRecordAtPoint().getPid() }));
        }
    };

    private Action acAddToBookmarks = new AbstractAction("Add to bookmarks") {
        @Override
        public void actionPerformed(ActionEvent e) {
            bookmarksController.markRecord(getRow());
        }
    };

    private BookmarksController bookmarksController;
    private FilterController filterController;

    private void setUpMenu() {

        JPopupMenu popupMenu = getMenu();
        popupMenu.add(acHideByTag);
        popupMenu.add(acHideByPid);
        popupMenu.addSeparator();
        popupMenu.add(acAddToBookmarks);
    }

    public LogRecordPopupMenuHandler(JTable table, final FilterController filterController,
            final BookmarksController bookmarksController) {
        super(table);
        this.filterController = filterController;
        this.bookmarksController = bookmarksController;
        setUpMenu();
    }

    private LogRecord getLogRecordAtPoint() {
        return ((LogRecordTableModel) getTable().getModel()).getRowData(getRow());
    }

}

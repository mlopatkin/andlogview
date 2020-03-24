/*
 * Copyright 2020 Mikhail Lopatkin
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

import com.google.common.collect.ImmutableList;

import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.SelectedRows;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TableRow;
import org.bitbucket.mlopatkin.utils.events.Observable;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

import javax.inject.Inject;

/**
 * Presenter for PopupMenu of the main Log Table.
 */
class TablePopupMenuPresenter {
    public interface TablePopupMenuView {
        void setCopyActionEnabled(boolean enabled);

        Observable<Runnable> setBookmarkAction(boolean enabled, String title);

        void show();
    }

    private final SelectedRows selectedRows;
    private final BookmarkModel bookmarkModel;

    @Inject
    public TablePopupMenuPresenter(SelectedRows selectedRows, BookmarkModel bookmarkModel) {
        this.selectedRows = selectedRows;
        this.bookmarkModel = bookmarkModel;
    }

    public void showContextMenu(TablePopupMenuView menuView, Column c, @Nullable TableRow row) {
        ImmutableList<TableRow> selectedRows = this.selectedRows.getSelectedRows();
        assert (row == null && selectedRows.isEmpty()) || (row != null && this.selectedRows.isRowSelected(
                row)) : "Selection wasn't adjusted";
        menuView.setCopyActionEnabled(!selectedRows.isEmpty());
        setUpBookmarkAction(menuView, selectedRows);
        menuView.show();
    }

    private void setUpBookmarkAction(TablePopupMenuView menuView, List<TableRow> selectedRows) {
        String addToBookmarksTitle = "Add to bookmarks";
        String removeFromBookmarksTitle = "Remove from bookmarks";

        boolean isBookmarkActionEnabled = (selectedRows.size() == 1);
        if (!isBookmarkActionEnabled) {
            menuView.setBookmarkAction(false, addToBookmarksTitle);
            return;
        }
        TableRow selectedRow = selectedRows.get(0);
        boolean isBookmarked = bookmarkModel.containsRecord(selectedRow.getRecord());
        if (isBookmarked) {
            menuView.setBookmarkAction(true, removeFromBookmarksTitle)
                    .addObserver(() -> removeFromBookmarks(selectedRow));
        } else {
            menuView.setBookmarkAction(true, addToBookmarksTitle).addObserver(() -> addToBookmarks(selectedRow));
        }
    }

    private void addToBookmarks(TableRow row) {
        bookmarkModel.addRecord(row.getRecord());
    }

    private void removeFromBookmarks(TableRow row) {
        bookmarkModel.removeRecord(row.getRecord());
    }
}

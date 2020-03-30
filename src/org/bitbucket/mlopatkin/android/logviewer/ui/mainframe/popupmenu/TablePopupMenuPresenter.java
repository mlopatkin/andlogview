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

package org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.popupmenu;

import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilteringMode;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.PopupMenuPresenter;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.SelectedRows;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TableRow;
import org.bitbucket.mlopatkin.utils.events.Observable;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

import javax.inject.Inject;

/**
 * Presenter for PopupMenu of the main Log Table.
 */
public class TablePopupMenuPresenter extends PopupMenuPresenter<TablePopupMenuPresenter.TablePopupMenuView> {
    public interface TablePopupMenuView extends PopupMenuPresenter.PopupMenuView {
        void setHeader(String columnName, String headerText);

        Observable<Runnable> setBookmarkAction(boolean enabled, String title);

        Observable<Runnable> addQuickFilterAction(boolean enabled, String title);
    }

    private final BookmarkModel bookmarkModel;

    @Inject
    public TablePopupMenuPresenter(SelectedRows selectedRows, BookmarkModel bookmarkModel) {
        super(selectedRows);
        this.bookmarkModel = bookmarkModel;
    }

    @Override
    protected void configureMenu(TablePopupMenuView view, Column c, @Nullable TableRow row, List<TableRow> selection) {
        setUpHeader(view, c, row);
        super.configureMenu(view, c, row, selection);
        setUpBookmarkAction(view, selection);
        setUpFilterActions(view, c, row);
    }

    private void setUpHeader(TablePopupMenuView view, Column c, @Nullable TableRow row) {
        if (row == null || c == Column.TIME || c == Column.INDEX) {
            return;
        }
        view.setHeader(ColumnData.getColumnTitleForHeader(c), ColumnData.getColumnValueForHeader(c, row));
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

    private void setUpFilterActions(TablePopupMenuView menuView, Column column, @Nullable TableRow row) {
        if (row == null || column == Column.TIME || column == Column.INDEX) {
            return;
        }
        for (FilteringMode filteringMode : FilteringMode.values()) {
            if (filteringMode != FilteringMode.HIGHLIGHT) {
                menuView.addQuickFilterAction(true, FilterData.getFilterMenuItemTitle(filteringMode, column));
            }
        }

    }

    private void addToBookmarks(TableRow row) {
        bookmarkModel.addRecord(row.getRecord());
    }

    private void removeFromBookmarks(TableRow row) {
        bookmarkModel.removeRecord(row.getRecord());
    }

    /**
     * Helper class that defines column names and how to format data for them in popup menu header.
     */
    private static class ColumnData {
        public static String getColumnValueForHeader(Column column, TableRow row) {
            if (column == Column.PRIORITY) {
                return String.valueOf(Column.PRIORITY.getValue(row.getRowIndex(), row.getRecord()));
            }
            return column.getStrValue(row.getRowIndex(), row.getRecord());
        }

        public static String getColumnTitleForHeader(Column column) {
            if (column == Column.MESSAGE) {
                return "msg";
            }
            return column.getColumnName();
        }
    }

    /** Helper class that defines titles for quick filter menu items */
    private static class FilterData {
        public static String getFilterMenuItemTitle(FilteringMode mode, Column column) {
            String columnName = ColumnData.getColumnTitleForHeader(column);
            // This enum is intended to be exhaustive
            switch (mode) {
                case SHOW:
                    return "Show only lines with this " + columnName;
                case HIDE:
                    return "Hide lines with this " + columnName;
                case HIGHLIGHT:
                    throw new IllegalArgumentException("HIGHLIGHT filter cannot have menu item");
                case WINDOW:
                    return "Show index window for lines with this " + columnName;
            }
            throw new IllegalArgumentException("Unexpected filtering mode " + mode);
        }
    }
}

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

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilteringMode;
import org.bitbucket.mlopatkin.android.logviewer.filters.HighlightColors;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterFromDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.PopupMenuPresenter;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.SelectedRows;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TableRow;
import org.bitbucket.mlopatkin.utils.events.Observable;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.inject.Inject;

/**
 * Presenter for PopupMenu of the main Log Table.
 */
public class TablePopupMenuPresenter extends PopupMenuPresenter<TablePopupMenuPresenter.TablePopupMenuView> {
    public interface TablePopupMenuView extends PopupMenuPresenter.PopupMenuView {
        void setHeader(String columnName, String headerText);

        Observable<Runnable> setBookmarkAction(boolean enabled, String title);

        Observable<Runnable> addQuickFilterDialogAction(String title);

        Observable<Runnable> addQuickFilterAction(String title);

        Observable<Consumer<Color>> addHighlightFilterAction(String title, List<Color> highlightColors);
    }

    private static final char ELLIPSIS = '\u2026';  // …

    private final BookmarkModel bookmarkModel;
    private final MenuFilterCreator filterCreator;
    private final HighlightColors highlightColors;

    @Inject
    public TablePopupMenuPresenter(SelectedRows selectedRows, BookmarkModel bookmarkModel,
            MenuFilterCreator filterCreator, HighlightColors highlightColors) {
        super(selectedRows);
        this.bookmarkModel = bookmarkModel;
        this.filterCreator = filterCreator;
        this.highlightColors = highlightColors;
    }

    @Override
    protected void configureMenu(TablePopupMenuView view, Column c, @Nullable TableRow row, List<TableRow> selection) {
        setUpHeader(view, c, row);
        setUpFilterActions(view, c, row);
        super.configureMenu(view, c, row, selection);
        setUpBookmarkAction(view, selection);
    }

    private void setUpHeader(TablePopupMenuView view, Column c, @Nullable TableRow row) {
        if (row == null || c == Column.INDEX) {
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
        if (row == null || column == Column.TIME || column == Column.INDEX || column == Column.TID) {
            return;
        }

        menuView.addQuickFilterDialogAction(FilterData.getDialogMenuItemTitle(column)).addObserver(
                () -> filterCreator.createFilterWithDialog(buildFilter(FilteringMode.getDefaultMode(), column, row)));

        for (FilteringMode filteringMode : FilteringMode.values()) {
            String itemTitle = FilterData.getFilterMenuItemTitle(filteringMode, column, row);
            if (filteringMode != FilteringMode.HIGHLIGHT) {
                menuView.addQuickFilterAction(itemTitle)
                        .addObserver(() -> addFilter(buildFilter(filteringMode, column, row)));
            } else {
                menuView.addHighlightFilterAction(itemTitle, highlightColors.getColors())
                        .addObserver(color -> addFilter(
                                buildFilter(FilteringMode.HIGHLIGHT, column, row).setHighlightColor(color)));
            }
        }

    }

    private FilterFromDialog buildFilter(FilteringMode mode, Column column, TableRow row) {
        FilterFromDialog filter = new FilterFromDialog().setMode(mode);
        ColumnData.applyColumnValueToFilter(filter, column, row);
        return filter;
    }

    private void addFilter(FilterFromDialog filter) {
        try {
            filter.initialize();
            filterCreator.addFilter(filter);
        } catch (RequestCompilationException e) {
            // It is possible that certain messages/app names will result in text being treated as a regex pattern.
            // Currently there is no support for escaping anything in the dialog.
            // TODO(mlopatkin) think about how handle this?
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

        public static void applyColumnValueToFilter(FilterFromDialog dialog, Column c, TableRow row) {
            LogRecord record = row.getRecord();
            switch (c) {
                case INDEX:
                case TIME:
                case TID:
                    throw new IllegalArgumentException("Cannot filter on " + c + " column");
                case PID:
                    dialog.setPids(Collections.singletonList(record.getPid()));
                    break;
                case APP_NAME:
                    dialog.setApps(Collections.singletonList(record.getAppName()));
                    break;
                case PRIORITY:
                    dialog.setPriority(record.getPriority());
                    break;
                case TAG:
                    dialog.setTags(Collections.singletonList(record.getTag()));
                    break;
                case MESSAGE:
                    dialog.setMessagePattern(record.getMessage());
                    break;
            }
        }
    }

    /** Helper class that defines titles for quick filter menu items */
    private static class FilterData {
        public static String getDialogMenuItemTitle(Column column) {
            return "Create filter for this " + column.getColumnName() + ELLIPSIS;
        }

        public static String getFilterMenuItemTitle(FilteringMode mode, Column column, TableRow row) {
            String columnName = column.getColumnName();
            if (column == Column.PRIORITY) {
                return getFilterMenuItemTitleForPriority(
                        mode, Objects.requireNonNull(Column.PRIORITY.getValue(row.getRowIndex(), row.getRecord())));
            }
            // This enum is intended to be exhaustive
            switch (mode) {
                case SHOW:
                    return "Show only this " + columnName;
                case HIDE:
                    return "Hide this " + columnName;
                case HIGHLIGHT:
                    return "Highlight this " + columnName;
                case WINDOW:
                    return "Show this " + columnName + " in index window";
            }
            throw new IllegalArgumentException("Unexpected filtering mode " + mode);
        }

        private static String getFilterMenuItemTitleForPriority(FilteringMode mode, Object value) {
            // This enum is intended to be exhaustive
            switch (mode) {
                case SHOW:
                    return "Show only priority >=" + value;
                case HIDE:
                    return "Hide priority >=" + value;
                case HIGHLIGHT:
                    return "Highlight priority >=" + value;
                case WINDOW:
                    return "Show priority >=" + value + " in index window";
            }
            throw new IllegalArgumentException("Unexpected filtering mode " + mode);
        }
    }
}

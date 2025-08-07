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

package name.mlopatkin.andlogview.ui.mainframe.popupmenu;

import name.mlopatkin.andlogview.ErrorDialogsHelper;
import name.mlopatkin.andlogview.bookmarks.BookmarkModel;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.search.RequestCompilationException;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialogData;
import name.mlopatkin.andlogview.ui.filterdialog.PatternsList;
import name.mlopatkin.andlogview.ui.filters.HighlightColors;
import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.PopupMenuPresenter;
import name.mlopatkin.andlogview.ui.logtable.SelectedRows;
import name.mlopatkin.andlogview.ui.logtable.TableRow;
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;
import name.mlopatkin.andlogview.utils.CommonChars;
import name.mlopatkin.andlogview.utils.events.Observable;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

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

    private static final Logger logger = LoggerFactory.getLogger(TablePopupMenuPresenter.class);

    private final BookmarkModel bookmarkModel;
    private final MenuFilterCreator filterCreator;
    private final HighlightColors highlightColors;
    private final DialogFactory dialogFactory;

    @Inject
    public TablePopupMenuPresenter(SelectedRows selectedRows, BookmarkModel bookmarkModel,
            MenuFilterCreator filterCreator, HighlightColors highlightColors, DialogFactory dialogFactory) {
        super(selectedRows);
        this.bookmarkModel = bookmarkModel;
        this.filterCreator = filterCreator;
        this.highlightColors = highlightColors;
        this.dialogFactory = dialogFactory;
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

        Object columnValue = column.getValue(row.getRowIndex(), row.getRecord());
        if (columnValue == null || (columnValue instanceof String && PatternsList.WHITESPACE.matchesAllOf(
                (String) columnValue))) {
            // Do not add filter actions for null/blank String columns. Until we can filter on 'em.
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

    private FilterFromDialogData buildFilter(FilteringMode mode, Column column, TableRow row) {
        FilterFromDialogData filterData = new FilterFromDialogData(mode);
        ColumnData.applyColumnValueToFilter(filterData, column, row);
        return filterData;
    }

    private void addFilter(FilterFromDialogData filterData) {
        try {
            filterCreator.addFilter(filterData.toFilter());
        } catch (RequestCompilationException e) {
            logger.error("Failed to add quick filter", e);
            ErrorDialogsHelper.showError(dialogFactory.getOwner(), "Failed to create filter. %s", e.getMessage());
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

        public static void applyColumnValueToFilter(FilterFromDialogData dialog, Column c, TableRow row) {
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
                    dialog.setApps(Collections.singletonList(escapeString(record.getAppName())));
                    break;
                case PRIORITY:
                    dialog.setPriority(record.getPriority());
                    break;
                case TAG:
                    dialog.setTags(Collections.singletonList(escapeString(record.getTag())));
                    break;
                case MESSAGE:
                    dialog.setMessagePattern(escapeString(record.getMessage()));
                    break;
            }
        }
    }

    /** Helper class that defines titles for quick filter menu items */
    private static class FilterData {
        public static String getDialogMenuItemTitle(Column column) {
            return "Create filter for this " + column.getColumnName() + CommonChars.ELLIPSIS;
        }

        public static String getFilterMenuItemTitle(FilteringMode mode, Column column, TableRow row) {
            String columnName = column.getColumnName();
            if (column == Column.PRIORITY) {
                return getFilterMenuItemTitleForPriority(
                        mode, Objects.requireNonNull(Column.PRIORITY.getValue(row.getRowIndex(), row.getRecord())));
            }
            // This enum is intended to be exhaustive
            return switch (mode) {
                case SHOW -> "Show only this " + columnName;
                case HIDE -> "Hide this " + columnName;
                case HIGHLIGHT -> "Highlight this " + columnName;
                case WINDOW -> "Show this " + columnName + " in index window";
            };
        }

        private static String getFilterMenuItemTitleForPriority(FilteringMode mode, Object value) {
            // This enum is intended to be exhaustive
            return switch (mode) {
                case SHOW -> "Show only priority >=" + value;
                case HIDE -> "Hide priority >=" + value;
                case HIGHLIGHT -> "Highlight priority >=" + value;
                case WINDOW -> "Show priority >=" + value + " in index window";
            };
        }
    }

    private static String escapeString(String text) {
        // This isn't the best possible way to deal with this but there is no way to force plain-textness of the pattern
        // yet.
        if (PatternsList.isRegex(text)) {
            return PatternsList.wrapRegex('^' + Pattern.quote(text) + '$');
        }
        return text;
    }
}

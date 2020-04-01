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

import com.google.common.collect.ImmutableList;

import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.filters.HighlightColors;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.SelectedRows;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TableRow;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TestSelectedRows;
import org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.popupmenu.FakeTablePopupMenuView.MenuElements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.Color;

import static org.bitbucket.mlopatkin.android.logviewer.test.TestData.RECORD1;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class TablePopupMenuPresenterTest {
    FakeTablePopupMenuView popupMenuView;
    BookmarkModel bookmarkModel = new BookmarkModel();
    @Mock
    MenuFilterCreator filterCreator;
    @Mock
    HighlightColors highlightColors;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        popupMenuView = new FakeTablePopupMenuView();
        when(highlightColors.getColors()).thenReturn(ImmutableList.of(Color.ORANGE, Color.BLUE, Color.RED));
    }

    @Test
    public void menuOrderTest() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        assertThat(popupMenuView.getMenuElements(),
                contains(
                        MenuElements.HEADER,
                        MenuElements.COPY_ACTION,
                        MenuElements.BOOKMARK_ACTION,
                        MenuElements.QUICK_FILTER_ACTION,
                        MenuElements.QUICK_FILTER_ACTION,
                        MenuElements.QUICK_FILTER_ACTION,
                        MenuElements.HIGHLIGHT_FILTER_ACTION));
    }

    @Test
    public void contextMenuIsShownIfSelectionEmpty() {
        TablePopupMenuPresenter presenter = createPresenter();

        presenter.showContextMenu(popupMenuView, Column.PID, null);
        assertTrue(popupMenuView.isShowing());
    }

    @Test
    public void contextMenuIsShownIfSelectionSingle() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        assertTrue(popupMenuView.isShowing());
    }

    @Test
    public void contextMenuIsShownIfSelectionMultiple() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(2));

        assertTrue(popupMenuView.isShowing());
    }

    @Test
    public void contextMenuHasCopyDisableIfSelectionEmpty() {
        TablePopupMenuPresenter presenter = createPresenter();
        presenter.showContextMenu(popupMenuView, Column.PID, null);

        assertFalse(popupMenuView.isCopyActionEnabled());
    }

    @Test
    public void contextMenuHasCopyEnabledIfSingleRowSelected() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        assertTrue(popupMenuView.isCopyActionEnabled());
    }

    @Test
    public void contextMenuHasCopyEnabledIfMultipleRowsSelected() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(2));

        assertTrue(popupMenuView.isCopyActionEnabled());
    }


    @Test
    public void bookmarkActionIsDisabledIfSelectionEmpty() {
        TablePopupMenuPresenter presenter = createPresenter();
        presenter.showContextMenu(popupMenuView, Column.PID, null);

        assertFalse(popupMenuView.isBookmarkActionEnabled());
    }

    @Test
    public void bookmarkActionIsEnabledIfSelectionSingle() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        assertTrue(popupMenuView.isBookmarkActionEnabled());
    }

    @Test
    public void bookmarkActionIsDisabledIfSelectionMultiple() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(2));

        assertFalse(popupMenuView.isBookmarkActionEnabled());
    }

    @Test
    public void bookmarkActionAddsRowToBookmarksIfNotAlready() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        popupMenuView.triggerBookmarkAction();

        assertTrue(bookmarkModel.containsRecord(RECORD1));
    }

    @Test
    public void bookmarkActionRemovesRowFromBookmarksIfAlreadyBookmarked() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        bookmarkModel.addRecord(RECORD1);

        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        popupMenuView.triggerBookmarkAction();

        assertFalse(bookmarkModel.containsRecord(RECORD1));
    }

    @Test
    public void headerIsNotShownIfClickedOnNoRow() {
        TablePopupMenuPresenter presenter = createPresenter();
        presenter.showContextMenu(popupMenuView, Column.PID, null);

        assertFalse(popupMenuView.isHeaderShowing());
    }

    @Test
    public void headerIsShownIfClickedOnSingleRow() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        assertTrue(popupMenuView.isHeaderShowing());
    }

    @Test
    public void headerIsShownIfClickedOnMultipleRows() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(2));

        assertTrue(popupMenuView.isHeaderShowing());
    }

    @Test
    public void quickFiltersAreAddedForSelectedRow() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));


        assertEquals(3, popupMenuView.getQuickFilterElementsCount());
    }

    @Test
    public void quickFiltersAreNotAddedIfRowIsNotSelected() {
        TablePopupMenuPresenter presenter = createPresenter();
        presenter.showContextMenu(popupMenuView, Column.PID, null);

        assertEquals(0, popupMenuView.getQuickFilterElementsCount());
    }

    @Test
    public void quickFiltersAreAddedIfMultipleRowsSelected() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        assertEquals(3, popupMenuView.getQuickFilterElementsCount());
    }

    private static TableRow makeRow(int index) {
        return new TableRow(index, RECORD1);
    }

    private TablePopupMenuPresenter createPresenter(TableRow... rows) {
        SelectedRows selectedRows = new TestSelectedRows(rows);
        return new TablePopupMenuPresenter(selectedRows, bookmarkModel, filterCreator, highlightColors);
    }

}

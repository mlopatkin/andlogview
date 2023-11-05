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

import static name.mlopatkin.andlogview.test.TestData.RECORD1;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import name.mlopatkin.andlogview.bookmarks.BookmarkModel;
import name.mlopatkin.andlogview.filters.HighlightColors;
import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.SelectedRows;
import name.mlopatkin.andlogview.ui.logtable.TableRow;
import name.mlopatkin.andlogview.ui.logtable.TestSelectedRows;
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;
import name.mlopatkin.andlogview.ui.mainframe.popupmenu.FakeTablePopupMenuView.MenuElements;

import com.google.common.collect.ImmutableList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.Color;

@ExtendWith(MockitoExtension.class)
public class TablePopupMenuPresenterTest {
    FakeTablePopupMenuView popupMenuView;
    final BookmarkModel bookmarkModel = new BookmarkModel();
    @Mock
    MenuFilterCreator filterCreator;
    @Mock
    HighlightColors highlightColors;
    @Mock
    DialogFactory dialogFactory;

    @BeforeEach
    public void setUp() throws Exception {
        popupMenuView = new FakeTablePopupMenuView();
        lenient().when(highlightColors.getColors()).thenReturn(ImmutableList.of(Color.ORANGE, Color.BLUE, Color.RED));
    }

    @Test
    public void menuOrderTest() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        assertThat(popupMenuView.getMenuElements(),
                contains(
                        MenuElements.HEADER,
                        MenuElements.QUICK_DIALOG_ACTION,
                        MenuElements.QUICK_FILTER_ACTION,
                        MenuElements.QUICK_FILTER_ACTION,
                        MenuElements.HIGHLIGHT_FILTER_ACTION,
                        MenuElements.QUICK_FILTER_ACTION,
                        MenuElements.COPY_ACTION,
                        MenuElements.BOOKMARK_ACTION
                ));
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

    @Test
    public void highlightFilterIsNotAddedIfNoRowsSelected() {
        TablePopupMenuPresenter presenter = createPresenter();
        presenter.showContextMenu(popupMenuView, Column.PID, null);

        assertFalse(popupMenuView.isHighlightActionAvailable());
    }

    @Test
    public void highlightFilterIsAddedIfSingleRowSelected() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        assertTrue(popupMenuView.isHighlightActionAvailable());
    }

    @Test
    public void highlightFilterIsAddedIfMultipleRowsSelected() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        assertTrue(popupMenuView.isHighlightActionAvailable());
    }

    @Test
    public void quickDialogActionIsNotAvailableIfNoRowsSelected() {
        TablePopupMenuPresenter presenter = createPresenter();
        presenter.showContextMenu(popupMenuView, Column.PID, null);

        assertFalse(popupMenuView.isQuickDialogActionAvailable());
    }

    @Test
    public void quickDialogActionIsAvailableIfSingleRowSelected() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        assertTrue(popupMenuView.isQuickDialogActionAvailable());
    }

    @Test
    public void quickDialogActionIsAvailableIfMultipleRowsSelected() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        assertTrue(popupMenuView.isQuickDialogActionAvailable());
    }

    private static TableRow makeRow(int index) {
        return new TableRow(index, RECORD1);
    }

    private TablePopupMenuPresenter createPresenter(TableRow... rows) {
        SelectedRows selectedRows = new TestSelectedRows(rows);
        return new TablePopupMenuPresenter(selectedRows, bookmarkModel, filterCreator, highlightColors, dialogFactory);
    }

}

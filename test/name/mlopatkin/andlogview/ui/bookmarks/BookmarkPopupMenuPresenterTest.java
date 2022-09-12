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

package name.mlopatkin.andlogview.ui.bookmarks;

import static name.mlopatkin.andlogview.test.TestData.RECORD1;
import static name.mlopatkin.andlogview.test.TestData.RECORD2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.bookmarks.BookmarkModel;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.ui.bookmarks.BookmarkPopupMenuPresenter.BookmarkPopupMenuView;
import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.SelectedRows;
import name.mlopatkin.andlogview.ui.logtable.TableRow;
import name.mlopatkin.andlogview.ui.logtable.TestSelectedRows;
import name.mlopatkin.andlogview.utils.events.Subject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

public class BookmarkPopupMenuPresenterTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    BookmarkPopupMenuView popupMenuView;
    BookmarkModel bookmarkModel = new BookmarkModel();

    Subject<Runnable> bookmarkDeleteAction = new Subject<>();

    @Before
    public void setUp() throws Exception {
        when(popupMenuView.setDeleteBookmarksActionEnabled(anyBoolean())).thenReturn(
                bookmarkDeleteAction.asObservable());

        bookmarkModel.addRecord(RECORD1);
        bookmarkModel.addRecord(RECORD2);
    }

    @Test
    public void contextMenuIsShownIfSelectionEmpty() {
        BookmarkPopupMenuPresenter presenter = createPresenter();

        presenter.showContextMenu(popupMenuView, Column.PID, null);
        verify(popupMenuView).show();
    }

    @Test
    public void contextMenuIsShownIfSelectionSingle() {
        BookmarkPopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        verify(popupMenuView).show();
    }

    @Test
    public void contextMenuIsShownIfSelectionMultiple() {
        BookmarkPopupMenuPresenter presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(2));

        verify(popupMenuView).show();
    }

    @Test
    public void contextMenuHasCopyDisableIfSelectionEmpty() {
        BookmarkPopupMenuPresenter presenter = createPresenter();
        presenter.showContextMenu(popupMenuView, Column.PID, null);

        verify(popupMenuView).setCopyActionEnabled(false);
        verify(popupMenuView, never()).setCopyActionEnabled(true);
    }

    @Test
    public void contextMenuHasCopyEnabledIfSingleRowSelected() {
        BookmarkPopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        verify(popupMenuView).setCopyActionEnabled(true);
        verify(popupMenuView, never()).setCopyActionEnabled(false);
    }

    @Test
    public void contextMenuHasCopyEnabledIfMultipleRowsSelected() {
        BookmarkPopupMenuPresenter presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(2));

        verify(popupMenuView).setCopyActionEnabled(true);
        verify(popupMenuView, never()).setCopyActionEnabled(false);
    }

    @Test
    public void contextMenuDisablesDeleteBookmarkActionIfNoRowsSelected() {
        BookmarkPopupMenuPresenter presenter = createPresenter();
        presenter.showContextMenu(popupMenuView, Column.PID, null);

        verify(popupMenuView).setDeleteBookmarksActionEnabled(false);
        verify(popupMenuView, never()).setDeleteBookmarksActionEnabled(true);
    }

    @Test
    public void contextMenuEnablesDeleteBookmarkActionIfSingleRowSelected() {
        BookmarkPopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        verify(popupMenuView).setDeleteBookmarksActionEnabled(true);
        verify(popupMenuView, never()).setDeleteBookmarksActionEnabled(false);
    }

    @Test
    public void contextMenuEnablesDeleteBookmarkActionIfSomeRowsSelected() {
        BookmarkPopupMenuPresenter presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        verify(popupMenuView).setDeleteBookmarksActionEnabled(true);
        verify(popupMenuView, never()).setDeleteBookmarksActionEnabled(false);
    }

    @Test
    public void contextMenuDeletingSingleRowRemovesItFromBookmarkModel() {
        BookmarkPopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));
        triggerBookmarkAction();

        assertFalse(bookmarkModel.containsRecord(RECORD1));
        assertTrue(bookmarkModel.containsRecord(RECORD2));
    }

    @Test
    public void contextMenuDeletingMultipleRowsRemovesThemFromBookmarkModel() {
        BookmarkPopupMenuPresenter presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));
        triggerBookmarkAction();

        assertFalse(bookmarkModel.containsRecord(RECORD1));
        assertFalse(bookmarkModel.containsRecord(RECORD2));
    }

    @Test
    public void contextMenuShowingDoesntTouchBookmarkModel() {
        BookmarkPopupMenuPresenter presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        assertTrue(bookmarkModel.containsRecord(RECORD1));
        assertTrue(bookmarkModel.containsRecord(RECORD2));
    }


    private BookmarkPopupMenuPresenter createPresenter(TableRow... rows) {
        SelectedRows selectedRows = new TestSelectedRows(rows);
        return new BookmarkPopupMenuPresenter(selectedRows, bookmarkModel);
    }

    private static TableRow makeRow(int index) {
        LogRecord[] records = {RECORD1, RECORD2};
        return new TableRow(index, records[index - 1]);
    }

    private void triggerBookmarkAction() {
        for (Runnable runnable : bookmarkDeleteAction) {
            runnable.run();
        }

    }
}

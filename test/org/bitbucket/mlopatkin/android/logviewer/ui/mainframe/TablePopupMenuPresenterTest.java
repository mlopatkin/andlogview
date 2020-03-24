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

import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.test.TestData;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.SelectedRows;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TableRow;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TestSelectedRows;
import org.bitbucket.mlopatkin.utils.events.Subject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TablePopupMenuPresenterTest {
    @Mock
    TablePopupMenuPresenter.TablePopupMenuView popupMenuView;
    BookmarkModel bookmarkModel = new BookmarkModel();

    Subject<Runnable> bookmarkAction = new Subject<>();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(popupMenuView.setBookmarkAction(anyBoolean(), any())).then(invocation -> bookmarkAction.asObservable());
    }

    @Test
    public void contextMenuIsShownIfSelectionEmpty() {
        TablePopupMenuPresenter presenter = createPresenter();

        presenter.showContextMenu(popupMenuView, Column.PID, null);
        verify(popupMenuView).show();
    }

    @Test
    public void contextMenuIsShownIfSelectionSingle() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        verify(popupMenuView).show();
    }

    @Test
    public void contextMenuIsShownIfSelectionMultiple() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(2));

        verify(popupMenuView).show();
    }

    @Test
    public void contextMenuHasCopyDisableIfSelectionEmpty() {
        TablePopupMenuPresenter presenter = createPresenter();
        presenter.showContextMenu(popupMenuView, Column.PID, null);

        verify(popupMenuView).setCopyActionEnabled(false);
        verify(popupMenuView, never()).setCopyActionEnabled(true);
    }

    @Test
    public void contextMenuHasCopyEnabledIfSingleRowSelected() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        verify(popupMenuView).setCopyActionEnabled(true);
        verify(popupMenuView, never()).setCopyActionEnabled(false);
    }

    @Test
    public void contextMenuHasCopyEnabledIfMultipleRowsSelected() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(2));

        verify(popupMenuView).setCopyActionEnabled(true);
        verify(popupMenuView, never()).setCopyActionEnabled(false);
    }


    @Test
    public void bookmarkActionIsDisabledIfSelectionEmpty() {
        TablePopupMenuPresenter presenter = createPresenter();
        presenter.showContextMenu(popupMenuView, Column.PID, null);

        verify(popupMenuView).setBookmarkAction(eq(false), any());
        verify(popupMenuView, never()).setBookmarkAction(eq(true), any());
    }

    @Test
    public void bookmarkActionIsEnabledIfSelectionSingle() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        verify(popupMenuView).setBookmarkAction(eq(true), any());
        verify(popupMenuView, never()).setBookmarkAction(eq(false), any());
    }

    @Test
    public void bookmarkActionIsDisabledIfSelectionMultiple() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(2));

        verify(popupMenuView).setBookmarkAction(eq(false), any());
        verify(popupMenuView, never()).setBookmarkAction(eq(true), any());
    }

    @Test
    public void bookmarkActionAddsRowToBookmarksIfNotAlready() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        triggerBookmarkAction();

        assertTrue(bookmarkModel.containsRecord(TestData.RECORD1));
    }

    @Test
    public void bookmarkActionRemovesRowFromBookmarksIfAlreadyBookmarked() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow(1));
        bookmarkModel.addRecord(TestData.RECORD1);

        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        triggerBookmarkAction();

        assertFalse(bookmarkModel.containsRecord(TestData.RECORD1));
    }

    private static TableRow makeRow(int index) {
        return new TableRow(index, TestData.RECORD1);
    }

    private TablePopupMenuPresenter createPresenter(TableRow... rows) {
        SelectedRows selectedRows = new TestSelectedRows(rows);
        return new TablePopupMenuPresenter(selectedRows, bookmarkModel);
    }

    private void triggerBookmarkAction() {
        for (Runnable runnable : bookmarkAction) {
            runnable.run();
        }
    }
}

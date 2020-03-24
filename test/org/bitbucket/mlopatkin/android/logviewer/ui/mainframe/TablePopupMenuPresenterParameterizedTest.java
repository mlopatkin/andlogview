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

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordParser;
import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.SelectedRows;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TableRow;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TestSelectedRows;
import org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.TablePopupMenuPresenter.ColumnData;
import org.bitbucket.mlopatkin.utils.events.Subject;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class TablePopupMenuPresenterParameterizedTest {
    public static final LogRecord RECORD = Objects.requireNonNull(LogRecordParser.parseThreadTime(null,
            "08-03 16:21:35.538    98   231 V AudioFlinger: start(4117)",
            Collections.singletonMap(98, "media_server")));
    @Mock
    TablePopupMenuPresenter.TablePopupMenuView popupMenuView;
    BookmarkModel bookmarkModel = new BookmarkModel();

    @Parameterized.Parameter
    public @MonotonicNonNull Column column;

    @Parameterized.Parameter(1)
    public boolean hasHeader;

    @Parameterized.Parameter(2)
    public @MonotonicNonNull String title;

    @Parameterized.Parameter(3)
    public @MonotonicNonNull String value;

    @Parameterized.Parameters(name = "{index}: {0}")
    public static List<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                // Column, has header
                {Column.INDEX, false, "", ""},
                {Column.TIME, false, "", ""},
                {Column.PID, true, "pid", "98"},
                {Column.TID, true, "tid", "231"},
                {Column.APP_NAME, true, "app", "media_server"},
                {Column.PRIORITY, true, "priority", "VERBOSE"},
                {Column.TAG, true, "tag", "AudioFlinger"},
                {Column.MESSAGE, true, "msg", "start(4117)"},
                });
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Subject<Runnable> bookmarkAction = new Subject<>();

        when(popupMenuView.setBookmarkAction(anyBoolean(), any())).then(invocation -> bookmarkAction.asObservable());
    }

    @Test
    public void headerIsShownIfNeeded() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow());
        presenter.showContextMenu(popupMenuView, column, makeRow());

        if (hasHeader) {
            verify(popupMenuView).setHeader(any(), any());
            verify(popupMenuView).setHeader(
                    ColumnData.getColumnTitleForHeader(column),
                    ColumnData.getColumnValueForHeader(column, makeRow()));
        } else {
            verify(popupMenuView, never()).setHeader(any(), any());
        }
    }

    private static TableRow makeRow() {
        return new TableRow(1, RECORD);
    }

    private TablePopupMenuPresenter createPresenter(TableRow... rows) {
        SelectedRows selectedRows = new TestSelectedRows(rows);
        return new TablePopupMenuPresenter(selectedRows, bookmarkModel);
    }
}

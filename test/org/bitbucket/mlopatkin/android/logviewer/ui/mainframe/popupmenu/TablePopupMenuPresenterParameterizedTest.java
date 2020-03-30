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
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordParser;
import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.SelectedRows;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TableRow;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TestSelectedRows;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TablePopupMenuPresenterParameterizedTest {
    public static final LogRecord RECORD = Objects.requireNonNull(LogRecordParser.parseThreadTime(null,
            "08-03 16:21:35.538    98   231 V AudioFlinger: start(4117)",
            Collections.singletonMap(98, "media_server")));
    FakeTablePopupMenuView popupMenuView;
    BookmarkModel bookmarkModel = new BookmarkModel();

    @Parameterized.Parameter
    public @MonotonicNonNull Column column;

    @Parameterized.Parameter(1)
    public boolean hasHeader;

    @Parameterized.Parameter(2)
    public @MonotonicNonNull String title;

    @Parameterized.Parameter(3)
    public @MonotonicNonNull String value;

    @Parameterized.Parameter(4)
    public int quickFiltersCount;

    @Parameterized.Parameters(name = "{index}: {0}")
    public static List<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                // Column, has header, header column, header value, quick filters count
                {Column.INDEX, false, "", "", 0},
                {Column.TIME, false, "", "", 0},
                {Column.PID, true, "pid", "98", 3},
                {Column.TID, true, "tid", "231", 3},
                {Column.APP_NAME, true, "app", "media_server", 3},
                {Column.PRIORITY, true, "priority", "VERBOSE", 3},
                {Column.TAG, true, "tag", "AudioFlinger", 3},
                {Column.MESSAGE, true, "msg", "start(4117)", 3},
                });
    }

    @Before
    public void setUp() throws Exception {
        popupMenuView = new FakeTablePopupMenuView();
    }

    @Test
    public void headerIsShownIfNeeded() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow());
        presenter.showContextMenu(popupMenuView, column, makeRow());

        if (hasHeader) {
            assertTrue(popupMenuView.isHeaderShowing());
            assertEquals(title, popupMenuView.getHeaderColumn());
            assertEquals(value, popupMenuView.getHeaderText());
        } else {
            assertFalse(popupMenuView.isHeaderShowing());
        }

        assertEquals(quickFiltersCount, popupMenuView.getQuickFilterElementsCount());
    }

    private static TableRow makeRow() {
        return new TableRow(1, RECORD);
    }

    private TablePopupMenuPresenter createPresenter(TableRow... rows) {
        SelectedRows selectedRows = new TestSelectedRows(rows);
        return new TablePopupMenuPresenter(selectedRows, bookmarkModel);
    }
}

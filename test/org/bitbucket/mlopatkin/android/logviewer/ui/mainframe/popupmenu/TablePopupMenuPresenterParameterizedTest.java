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
import org.bitbucket.mlopatkin.android.logviewer.filters.FilteringMode;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterFromDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.SelectedRows;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TableRow;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TestSelectedRows;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hamcrest.Matcher;
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

import static org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterMatchers.hasApps;
import static org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterMatchers.hasMessage;
import static org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterMatchers.hasMode;
import static org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterMatchers.hasPids;
import static org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterMatchers.hasPriority;
import static org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterMatchers.hasTags;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@RunWith(Parameterized.class)
public class TablePopupMenuPresenterParameterizedTest {
    // TODO(mlopatkin) Split this up after migrating to JUnit 5

    public static final LogRecord RECORD = Objects.requireNonNull(LogRecordParser.parseThreadTime(null,
            "08-03 16:21:35.538    98   231 V AudioFlinger: start(4117)",
            Collections.singletonMap(98, "media_server")));
    FakeTablePopupMenuView popupMenuView;
    BookmarkModel bookmarkModel = new BookmarkModel();
    @Mock
    MenuFilterCreator filterCreator;

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

    @Parameterized.Parameter(5)
    public @Nullable Matcher<FilterFromDialog> filterMatcher;

    @Parameterized.Parameters(name = "{index}: {0}")
    public static List<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                // Column, has header, header column, header value, quick filters count, quick filter matcher
                {Column.INDEX, false, "", "", 0, null},
                {Column.TIME, false, "", "", 0, null},
                {Column.PID, true, "pid", "98", 3, hasPids(contains(98))},
                {Column.TID, true, "tid", "231", 0, null},
                {Column.APP_NAME, true, "app", "media_server", 3, hasApps(contains("media_server"))},
                {Column.PRIORITY, true, "priority", "VERBOSE", 3, hasPriority(equalTo(LogRecord.Priority.VERBOSE))},
                {Column.TAG, true, "tag", "AudioFlinger", 3, hasTags(contains("AudioFlinger"))},
                {Column.MESSAGE, true, "msg", "start(4117)", 3, hasMessage(equalTo("start(4117)"))},
                });
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
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
    }

    @Test
    public void testShowFilterAction() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow());
        presenter.showContextMenu(popupMenuView, column, makeRow());

        assertEquals(quickFiltersCount, popupMenuView.getQuickFilterElementsCount());

        if (filterMatcher != null) {
            popupMenuView.triggerQuickFilterAction(0);

            verify(filterCreator).addFilter(argThat(both(hasMode(equalTo(FilteringMode.SHOW))).and(filterMatcher)));
        }
    }

    @Test
    public void testHideFilterAction() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow());
        presenter.showContextMenu(popupMenuView, column, makeRow());

        assertEquals(quickFiltersCount, popupMenuView.getQuickFilterElementsCount());

        if (filterMatcher != null) {
            popupMenuView.triggerQuickFilterAction(1);

            verify(filterCreator).addFilter(argThat(both(hasMode(equalTo(FilteringMode.HIDE))).and(filterMatcher)));
        }
    }

    @Test
    public void testIndexFilterAction() {
        TablePopupMenuPresenter presenter = createPresenter(makeRow());
        presenter.showContextMenu(popupMenuView, column, makeRow());

        assertEquals(quickFiltersCount, popupMenuView.getQuickFilterElementsCount());

        if (filterMatcher != null) {
            popupMenuView.triggerQuickFilterAction(2);

            verify(filterCreator).addFilter(argThat(both(hasMode(equalTo(FilteringMode.WINDOW))).and(filterMatcher)));
        }
    }

    private static TableRow makeRow() {
        return new TableRow(1, RECORD);
    }

    private TablePopupMenuPresenter createPresenter(TableRow... rows) {
        SelectedRows selectedRows = new TestSelectedRows(rows);
        return new TablePopupMenuPresenter(selectedRows, bookmarkModel, filterCreator);
    }
}

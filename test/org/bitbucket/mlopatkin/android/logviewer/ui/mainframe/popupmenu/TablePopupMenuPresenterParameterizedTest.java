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

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordParser;
import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilteringMode;
import org.bitbucket.mlopatkin.android.logviewer.filters.HighlightColors;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterFromDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.SelectedRows;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TableRow;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TestSelectedRows;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.Color;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

import static org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterMatchers.hasApps;
import static org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterMatchers.hasColor;
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
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class TablePopupMenuPresenterParameterizedTest {
    public static final LogRecord RECORD = Objects.requireNonNull(LogRecordParser.parseThreadTime(null,
            "08-03 16:21:35.538    98   231 V AudioFlinger: start(4117)",
            Collections.singletonMap(98, "media_server")));
    FakeTablePopupMenuView popupMenuView;
    BookmarkModel bookmarkModel = new BookmarkModel();
    @Mock
    MenuFilterCreator filterCreator;
    @Mock
    HighlightColors highlightColors;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        popupMenuView = new FakeTablePopupMenuView();
        when(highlightColors.getColors()).thenReturn(ImmutableList.of(Color.ORANGE, Color.BLUE, Color.RED));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "TIME, time, 08-03 16:21:35.538",
            "PID, pid, 98",
            "TID, tid, 231",
            "APP_NAME, app, media_server",
            "PRIORITY, priority, VERBOSE",
            "TAG, tag, AudioFlinger",
            "MESSAGE, msg, start(4117)",
    })
    public void headerIsShownForColumn(Column column, String headerColumn, String headerValue) {
        TablePopupMenuPresenter presenter = createPresenter(makeRow());
        presenter.showContextMenu(popupMenuView, column, makeRow());

        assertTrue(popupMenuView.isHeaderShowing());
        assertEquals(headerColumn, popupMenuView.getHeaderColumn());
        assertEquals(headerValue, popupMenuView.getHeaderText());
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = "INDEX")
    void headerIsNotShownForColumn(Column column) {
        TablePopupMenuPresenter presenter = createPresenter(makeRow());
        presenter.showContextMenu(popupMenuView, column, makeRow());

        assertFalse(popupMenuView.isHeaderShowing());
    }

    private static Object[][] getColumnsWithFilters() {
        return new Object[][] {
                {Column.PID, hasPids(contains(98))},
                {Column.APP_NAME, hasApps(contains("media_server"))},
                {Column.PRIORITY, hasPriority(equalTo(LogRecord.Priority.VERBOSE))},
                {Column.TAG, hasTags(contains("AudioFlinger"))},
                {Column.MESSAGE, hasMessage(equalTo("start(4117)"))}
        };
    }

    private static Object[][] getModesWithActionIndex() {
        return new Object[][] {
                {0, FilteringMode.SHOW},
                {1, FilteringMode.HIDE},
                {2, FilteringMode.WINDOW},
                };
    }

    private static Stream<Arguments> filterActionParams() {
        return Stream.of(getModesWithActionIndex()).flatMap(
                modesParams -> Stream.of(getColumnsWithFilters()).map(
                        columnParams -> Arguments.of(columnParams[0], columnParams[1], modesParams[0], modesParams[1])
                ));
    }

    @ParameterizedTest(name = "{0}/{3}")
    @MethodSource("filterActionParams")
    public void testFilterAction(Column column, Matcher<FilterFromDialog> filterMatcher, int actionIndex,
            FilteringMode mode) {
        TablePopupMenuPresenter presenter = createPresenter(makeRow());
        presenter.showContextMenu(popupMenuView, column, makeRow());

        popupMenuView.triggerQuickFilterAction(actionIndex);

        verify(filterCreator).addFilter(argThat(both(hasMode(equalTo(mode))).and(filterMatcher)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getColumnsWithFilters")
    public void testHighlightAction(Column column, Matcher<FilterFromDialog> filterMatcher) {
        TablePopupMenuPresenter presenter = createPresenter(makeRow());
        presenter.showContextMenu(popupMenuView, column, makeRow());

        popupMenuView.triggerHighlightAction(1);

        verify(filterCreator).addFilter(
                argThat(both(hasMode(equalTo(FilteringMode.HIGHLIGHT))).and(hasColor(equalTo(Color.BLUE)))
                        .and(filterMatcher)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getColumnsWithFilters")
    public void quickFilterDialogActionOpensDialog(Column column, Matcher<FilterFromDialog> filterMatcher) {
        TablePopupMenuPresenter presenter = createPresenter(makeRow());
        presenter.showContextMenu(popupMenuView, column, makeRow());

        popupMenuView.triggerQuickDialogAction();

        verify(filterCreator).createFilterWithDialog(argThat(filterMatcher));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"INDEX", "TIME", "TID"})
    public void testFilterActionIsNotAvailable(Column column) {
        TablePopupMenuPresenter presenter = createPresenter(makeRow());
        presenter.showContextMenu(popupMenuView, column, makeRow());

        assertEquals(0, popupMenuView.getQuickFilterElementsCount());
        assertFalse(popupMenuView.isHighlightActionAvailable());
        assertFalse(popupMenuView.isQuickDialogActionAvailable());
    }

    private static TableRow makeRow() {
        return new TableRow(1, RECORD);
    }

    static Stream<Arguments> getRegexishRowArgs() {
        return Stream.of(
                arguments(Column.APP_NAME, hasApps(contains("/^\\Q/usr/bin/[/\\E$/"))),
                arguments(Column.TAG, hasTags(contains("/^\\Q/Broken(/\\E$/"))),
                arguments(Column.MESSAGE, hasMessage(equalTo("/^\\Q/Broken \\E\\\\E\\Q[Message/\\E$/")))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getRegexishRowArgs")
    public void filterIsSuccessfullyCreatedIfRegexishRowSelected(Column column,
            Matcher<FilterFromDialog> matchesFilter) {
        TablePopupMenuPresenter presenter = createPresenter(makeRegexishRow());
        presenter.showContextMenu(popupMenuView, column, makeRegexishRow());
        popupMenuView.triggerQuickFilterAction(0);

        verify(filterCreator).addFilter(argThat(matchesFilter));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getRegexishRowArgs")
    public void filterDialogIsSuccessfullyOpenedIfRegexishRowSelected(Column column,
            Matcher<FilterFromDialog> matchesFilter) {
        TablePopupMenuPresenter presenter = createPresenter(makeRegexishRow());
        presenter.showContextMenu(popupMenuView, column, makeRegexishRow());
        popupMenuView.triggerQuickDialogAction();

        verify(filterCreator).createFilterWithDialog(argThat(matchesFilter));
    }

    private static TableRow makeRegexishRow() {
        LogRecord record = new LogRecord(null, 123, 456, "/usr/bin/[/", LogRecord.Priority.INFO, "/Broken(/",
                "/Broken \\E[Message/");
        return new TableRow(1, record);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = Column.class, names = {"APP_NAME", "TAG", "MESSAGE"})
    public void filterOptionsNotAvailableIfCellIsEmpty(Column column) {
        TableRow row = makeValueRow("");
        TablePopupMenuPresenter presenter = createPresenter(row);
        presenter.showContextMenu(popupMenuView, column, row);

        assertFalse(popupMenuView.isQuickDialogActionAvailable());
        assertFalse(popupMenuView.isHighlightActionAvailable());
        assertEquals(0, popupMenuView.getQuickFilterElementsCount());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = Column.class, names = {"APP_NAME", "TAG", "MESSAGE"})
    public void filterOptionsNotAvailableIfCellIsBlank(Column column) {
        TableRow row = makeValueRow("  ");
        TablePopupMenuPresenter presenter = createPresenter(row);
        presenter.showContextMenu(popupMenuView, column, row);

        assertFalse(popupMenuView.isQuickDialogActionAvailable());
        assertFalse(popupMenuView.isHighlightActionAvailable());
        assertEquals(0, popupMenuView.getQuickFilterElementsCount());
    }

    private static TableRow makeValueRow(String value) {
        LogRecord record = new LogRecord(null, 123, 456, value, LogRecord.Priority.INFO, value, value);
        return new TableRow(1, record);
    }

    private TablePopupMenuPresenter createPresenter(TableRow... rows) {
        SelectedRows selectedRows = new TestSelectedRows(rows);
        return new TablePopupMenuPresenter(selectedRows, bookmarkModel, filterCreator, highlightColors);
    }
}

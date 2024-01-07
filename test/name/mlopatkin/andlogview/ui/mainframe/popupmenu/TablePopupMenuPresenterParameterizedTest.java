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

import static name.mlopatkin.andlogview.ui.filterdialog.FilterMatchers.hasApps;
import static name.mlopatkin.andlogview.ui.filterdialog.FilterMatchers.hasColor;
import static name.mlopatkin.andlogview.ui.filterdialog.FilterMatchers.hasMessage;
import static name.mlopatkin.andlogview.ui.filterdialog.FilterMatchers.hasMode;
import static name.mlopatkin.andlogview.ui.filterdialog.FilterMatchers.hasPids;
import static name.mlopatkin.andlogview.ui.filterdialog.FilterMatchers.hasPriority;
import static name.mlopatkin.andlogview.ui.filterdialog.FilterMatchers.hasTags;

import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import name.mlopatkin.andlogview.bookmarks.BookmarkModel;
import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecordUtils;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filters.HighlightColors;
import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.SelectedRows;
import name.mlopatkin.andlogview.ui.logtable.TableRow;
import name.mlopatkin.andlogview.ui.logtable.TestSelectedRows;
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;

import com.google.common.collect.ImmutableList;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.Color;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class TablePopupMenuPresenterParameterizedTest {
    static final LogRecord RECORD = LogRecordUtils.forTimestamp("08-03 16:21:35.538")
            .withPid(98)
            .withAppName("media_server")
            .withTid(231)
            .withTag("AudioFlinger")
            .withMessage("start(4117)");

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

        Assertions.assertTrue(popupMenuView.isHeaderShowing());
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

    @SuppressWarnings("unused")
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
        LogRecord record = LogRecordUtils.forMessage("/Broken \\E[Message/")
                .withAppName("/usr/bin/[/")
                .withTag("/Broken(/");

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
        LogRecord record = LogRecordUtils.forMessage(value)
                .withAppName(value)
                .withTag(value);
        return new TableRow(1, record);
    }

    private TablePopupMenuPresenter createPresenter(TableRow... rows) {
        SelectedRows selectedRows = new TestSelectedRows(rows);
        return new TablePopupMenuPresenter(selectedRows, bookmarkModel, filterCreator, highlightColors, dialogFactory);
    }
}

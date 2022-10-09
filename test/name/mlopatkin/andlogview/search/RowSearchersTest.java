package name.mlopatkin.andlogview.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecordUtils;
import name.mlopatkin.andlogview.ui.logtable.Column;

import org.junit.Test;
import org.mockito.Mockito;

public class RowSearchersTest {
    static final String TAG_1 = "Tag1";
    static final String TAG_WITH_SPACE = "Tag with space";
    static final String TAG_CONTACTS = "contacts";

    static final String APP_CONTACTS = "com.android.contacts";
    static final String APP_NAME_SYSTEM_SERVER = "system_server";
    static final String APP_NAME_SF = "/system/bin/surfaceflinger";

    static final String MSG_CONTACTS = "Starting activity com.android.contacts";
    static final String MSG_SYSTEM_SERVER = "System_server died";

    private static LogRecord makeRecord(String tag, String app, String msg) {
        return LogRecordUtils.forTag(tag).withAppName(app).withMessage(msg);
    }

    @Test
    public void testTagSearcher_search() throws Exception {
        LogRecord record = makeRecord(TAG_CONTACTS, APP_NAME_SF, MSG_SYSTEM_SERVER);
        LogRecord recordNotMatch = makeRecord(TAG_1, APP_NAME_SF, MSG_CONTACTS);

        RowSearchStrategy searcher =
                new ValueSearcher(SearchStrategyFactory.createHighlightStrategy("contacts"), Column.TAG);
        assertTrue(searcher.isRowMatched(record));
        assertFalse(searcher.isRowMatched(recordNotMatch));
    }

    @Test
    public void testTagSearcher_highlight() throws Exception {
        LogRecord record = makeRecord(TAG_CONTACTS, APP_CONTACTS, MSG_CONTACTS);
        RowSearchStrategy searcher =
                new ValueSearcher(SearchStrategyFactory.createHighlightStrategy("contacts"), Column.TAG);

        TextHighlighter mockHighlighterTag = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Column.TAG.getIndex(), mockHighlighterTag);
        verify(mockHighlighterTag).highlightText(0, TAG_CONTACTS.length());

        TextHighlighter mockHighlighterMsg = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Column.MESSAGE.getIndex(), mockHighlighterMsg);
        verifyNoInteractions(mockHighlighterMsg);

        TextHighlighter mockHighlighterAppName = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Column.APP_NAME.getIndex(), mockHighlighterAppName);
        verifyNoInteractions(mockHighlighterAppName);
    }

    @Test
    public void testMsgSearcher() throws Exception {
        LogRecord record = makeRecord(TAG_1, APP_NAME_SF, MSG_CONTACTS);
        LogRecord recordNotMatch = makeRecord(TAG_CONTACTS, APP_CONTACTS, MSG_SYSTEM_SERVER);

        RowSearchStrategy searcher =
                new ValueSearcher(SearchStrategyFactory.createHighlightStrategy("contacts"), Column.MESSAGE);
        assertTrue(searcher.isRowMatched(record));
        assertFalse(searcher.isRowMatched(recordNotMatch));
    }

    @Test
    public void testMsgSearcher_highlight() throws Exception {
        LogRecord record = makeRecord(TAG_CONTACTS, APP_CONTACTS, MSG_CONTACTS);
        RowSearchStrategy searcher =
                new ValueSearcher(SearchStrategyFactory.createHighlightStrategy("contacts"), Column.MESSAGE);

        TextHighlighter mockHighlighterTag = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Column.TAG.getIndex(), mockHighlighterTag);
        verifyNoInteractions(mockHighlighterTag);
        TextHighlighter mockHighlighterMsg = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Column.MESSAGE.getIndex(), mockHighlighterMsg);
        verify(mockHighlighterMsg).highlightText(Mockito.anyInt(), Mockito.anyInt());

        TextHighlighter mockHighlighterAppName = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Column.APP_NAME.getIndex(), mockHighlighterAppName);
        verifyNoInteractions(mockHighlighterAppName);
    }

    @Test
    public void testAppNameSearcher() throws Exception {
        LogRecord record = makeRecord(TAG_1, APP_CONTACTS, MSG_SYSTEM_SERVER);
        LogRecord recordNotMatch = makeRecord(TAG_CONTACTS, APP_NAME_SF, MSG_CONTACTS);

        RowSearchStrategy searcher =
                new ValueSearcher(SearchStrategyFactory.createHighlightStrategy("contacts"), Column.APP_NAME);
        assertTrue(searcher.isRowMatched(record));
        assertFalse(searcher.isRowMatched(recordNotMatch));
    }

    @Test
    public void testAppNameSearcher_highlight() throws Exception {
        LogRecord record = makeRecord(TAG_CONTACTS, APP_CONTACTS, MSG_CONTACTS);
        RowSearchStrategy searcher =
                new ValueSearcher(SearchStrategyFactory.createHighlightStrategy("contacts"), Column.APP_NAME);

        TextHighlighter mockHighlighterTag = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Column.TAG.getIndex(), mockHighlighterTag);
        verifyNoInteractions(mockHighlighterTag);

        TextHighlighter mockHighlighterMsg = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Column.MESSAGE.getIndex(), mockHighlighterMsg);
        verifyNoInteractions(mockHighlighterMsg);

        TextHighlighter mockHighlighterAppName = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Column.APP_NAME.getIndex(), mockHighlighterAppName);
        verify(mockHighlighterAppName).highlightText(Mockito.anyInt(), Mockito.anyInt());
    }
}

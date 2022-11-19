/*
 * Copyright 2013 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import name.mlopatkin.andlogview.logmodel.Field;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecordUtils;
import name.mlopatkin.andlogview.search.text.SearchStrategyFactory;
import name.mlopatkin.andlogview.search.text.TextHighlighter;

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
                new ValueSearcher(SearchStrategyFactory.createHighlightStrategy("contacts"), Field.TAG);
        assertTrue(searcher.test(record));
        assertFalse(searcher.test(recordNotMatch));
    }

    @Test
    public void testTagSearcher_highlight() throws Exception {
        LogRecord record = makeRecord(TAG_CONTACTS, APP_CONTACTS, MSG_CONTACTS);
        RowSearchStrategy searcher =
                new ValueSearcher(SearchStrategyFactory.createHighlightStrategy("contacts"), Field.TAG);

        TextHighlighter mockHighlighterTag = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Field.TAG, mockHighlighterTag);
        verify(mockHighlighterTag).highlightText(0, TAG_CONTACTS.length());

        TextHighlighter mockHighlighterMsg = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Field.MESSAGE, mockHighlighterMsg);
        verifyNoInteractions(mockHighlighterMsg);

        TextHighlighter mockHighlighterAppName = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Field.APP_NAME, mockHighlighterAppName);
        verifyNoInteractions(mockHighlighterAppName);
    }

    @Test
    public void testMsgSearcher() throws Exception {
        LogRecord record = makeRecord(TAG_1, APP_NAME_SF, MSG_CONTACTS);
        LogRecord recordNotMatch = makeRecord(TAG_CONTACTS, APP_CONTACTS, MSG_SYSTEM_SERVER);

        RowSearchStrategy searcher =
                new ValueSearcher(SearchStrategyFactory.createHighlightStrategy("contacts"), Field.MESSAGE);
        assertTrue(searcher.test(record));
        assertFalse(searcher.test(recordNotMatch));
    }

    @Test
    public void testMsgSearcher_highlight() throws Exception {
        LogRecord record = makeRecord(TAG_CONTACTS, APP_CONTACTS, MSG_CONTACTS);
        RowSearchStrategy searcher =
                new ValueSearcher(SearchStrategyFactory.createHighlightStrategy("contacts"), Field.MESSAGE);

        TextHighlighter mockHighlighterTag = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Field.TAG, mockHighlighterTag);
        verifyNoInteractions(mockHighlighterTag);
        TextHighlighter mockHighlighterMsg = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Field.MESSAGE, mockHighlighterMsg);
        verify(mockHighlighterMsg).highlightText(Mockito.anyInt(), Mockito.anyInt());

        TextHighlighter mockHighlighterAppName = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Field.APP_NAME, mockHighlighterAppName);
        verifyNoInteractions(mockHighlighterAppName);
    }

    @Test
    public void testAppNameSearcher() throws Exception {
        LogRecord record = makeRecord(TAG_1, APP_CONTACTS, MSG_SYSTEM_SERVER);
        LogRecord recordNotMatch = makeRecord(TAG_CONTACTS, APP_NAME_SF, MSG_CONTACTS);

        RowSearchStrategy searcher =
                new ValueSearcher(SearchStrategyFactory.createHighlightStrategy("contacts"), Field.APP_NAME);
        assertTrue(searcher.test(record));
        assertFalse(searcher.test(recordNotMatch));
    }

    @Test
    public void testAppNameSearcher_highlight() throws Exception {
        LogRecord record = makeRecord(TAG_CONTACTS, APP_CONTACTS, MSG_CONTACTS);
        RowSearchStrategy searcher =
                new ValueSearcher(SearchStrategyFactory.createHighlightStrategy("contacts"), Field.APP_NAME);

        TextHighlighter mockHighlighterTag = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Field.TAG, mockHighlighterTag);
        verifyNoInteractions(mockHighlighterTag);

        TextHighlighter mockHighlighterMsg = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Field.MESSAGE, mockHighlighterMsg);
        verifyNoInteractions(mockHighlighterMsg);

        TextHighlighter mockHighlighterAppName = Mockito.mock(TextHighlighter.class);
        searcher.highlightColumn(record, Field.APP_NAME, mockHighlighterAppName);
        verify(mockHighlighterAppName).highlightText(Mockito.anyInt(), Mockito.anyInt());
    }
}

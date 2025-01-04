/*
 * Copyright 2015 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.filters;

import static name.mlopatkin.andlogview.filters.ToggleFilter.hide;
import static name.mlopatkin.andlogview.test.TestData.MATCH_ALL;
import static name.mlopatkin.andlogview.test.TestData.RECORD1;
import static name.mlopatkin.andlogview.test.TestData.RECORD2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.test.TestData;

import org.junit.Test;

import java.awt.Color;
import java.util.function.Predicate;

public class LogRecordHighlighterTest {
    private final MutableFilterModel model = MutableFilterModel.create();
    private final LogRecordHighlighter highlighter = new LogRecordHighlighter(model);

    private static final Color COLOR1 = Color.RED;
    private static final Color COLOR2 = Color.BLUE;
    private static final Color COLOR3 = Color.BLACK;

    private static final ColoringFilter MATCH_FIRST_COLOR1 = makeColorFilter(TestData.MATCH_FIRST, COLOR1);
    private static final ColoringFilter MATCH_FIRST_COLOR2 = makeColorFilter(TestData.MATCH_FIRST, COLOR2);
    private static final ColoringFilter MATCH_FIRST_COLOR3 = makeColorFilter(TestData.MATCH_FIRST, COLOR3);
    private static final ColoringFilter MATCH_ALL_COLOR1 = makeColorFilter(TestData.MATCH_ALL, COLOR1);

    @Test
    public void testDefault() throws Exception {
        assertNull(highlighter.getColor(RECORD1));
        assertNull(highlighter.getColor(RECORD2));
    }

    @Test
    public void testSimpleColor() throws Exception {
        model.addFilter(MATCH_FIRST_COLOR1);
        assertEquals(COLOR1, highlighter.getColor(RECORD1));
        assertNull(highlighter.getColor(RECORD2));
    }

    @Test
    public void testOrderMatters() throws Exception {
        model.addFilter(MATCH_ALL_COLOR1);
        model.addFilter(MATCH_FIRST_COLOR2);
        assertEquals(COLOR2, highlighter.getColor(RECORD1));
        assertEquals(COLOR1, highlighter.getColor(RECORD2));
    }

    @Test
    public void testRemove() throws Exception {
        model.addFilter(MATCH_ALL_COLOR1);
        model.addFilter(MATCH_FIRST_COLOR2);

        model.removeFilter(MATCH_FIRST_COLOR2);
        assertEquals(COLOR1, highlighter.getColor(RECORD1));
        assertEquals(COLOR1, highlighter.getColor(RECORD2));

        model.removeFilter(MATCH_ALL_COLOR1);
        assertNull(highlighter.getColor(RECORD1));
        assertNull(highlighter.getColor(RECORD2));
    }

    @Test
    public void testReplace() throws Exception {
        model.addFilter(MATCH_FIRST_COLOR1);

        assertEquals(COLOR1, highlighter.getColor(RECORD1));

        model.replaceFilter(MATCH_FIRST_COLOR1, MATCH_FIRST_COLOR2);

        assertEquals(COLOR2, highlighter.getColor(RECORD1));
    }

    @Test
    public void testReplaceDoesntBringToBack() throws Exception {
        model.addFilter(MATCH_FIRST_COLOR2);
        model.addFilter(MATCH_ALL_COLOR1);

        assertEquals(COLOR1, highlighter.getColor(RECORD1));
        assertEquals(COLOR1, highlighter.getColor(RECORD2));

        model.replaceFilter(MATCH_FIRST_COLOR2, MATCH_FIRST_COLOR3);

        assertEquals(COLOR1, highlighter.getColor(RECORD1));

        // sanity check
        model.removeFilter(MATCH_ALL_COLOR1);
        assertEquals(COLOR3, highlighter.getColor(RECORD1));
    }

    @Test
    public void testEnable() throws Exception {
        model.addFilter(MATCH_FIRST_COLOR1);
        model.replaceFilter(MATCH_FIRST_COLOR1, MATCH_FIRST_COLOR1.disabled());
        assertNull(highlighter.getColor(RECORD1));

        model.replaceFilter(MATCH_FIRST_COLOR1.disabled(), MATCH_FIRST_COLOR1.enabled());
        assertEquals(COLOR1, highlighter.getColor(RECORD1));
    }

    @Test
    public void testEnableDoesntBringToBack() throws Exception {
        model.addFilter(MATCH_FIRST_COLOR2);
        model.addFilter(MATCH_ALL_COLOR1);

        assertEquals(COLOR1, highlighter.getColor(RECORD1));

        model.replaceFilter(MATCH_FIRST_COLOR2, MATCH_FIRST_COLOR2.disabled());
        model.replaceFilter(MATCH_FIRST_COLOR2.disabled(), MATCH_FIRST_COLOR2.enabled());

        assertEquals(COLOR1, highlighter.getColor(RECORD1));
    }

    @Test
    public void addingColorFilterNotifiesObserver() {
        LogRecordHighlighter.Observer obs = mock();

        highlighter.asObservable().addObserver(obs);
        model.addFilter(MATCH_ALL_COLOR1);

        verify(obs).onFiltersChanged();
    }

    @Test
    public void addingNonColorFilterDoesNotNotifyObserver() {
        LogRecordHighlighter.Observer obs = mock();

        highlighter.asObservable().addObserver(obs);
        model.addFilter(hide(MATCH_ALL));

        verify(obs, never()).onFiltersChanged();
    }

    @Test
    public void movingFilterChangesColor() {
        var red = makeColorFilter(MATCH_ALL, Color.RED);
        var blue = makeColorFilter(MATCH_ALL, Color.BLUE);

        model.addFilter(red);
        model.addFilter(blue);

        assertEquals(Color.BLUE, highlighter.getColor(RECORD1));

        LogRecordHighlighter.Observer obs = mock();
        highlighter.asObservable().addObserver(obs);

        model.insertFilterBefore(blue, red);

        verify(obs).onFiltersChanged();
        assertEquals(Color.RED, highlighter.getColor(RECORD1));
    }

    private static ColoringFilter makeColorFilter(final Predicate<LogRecord> base, final Color c) {
        return new ColoringToggleFilter(c, true, base);
    }
}

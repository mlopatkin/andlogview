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

import static name.mlopatkin.andlogview.test.TestData.RECORD1;
import static name.mlopatkin.andlogview.test.TestData.RECORD2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.test.TestData;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Before;
import org.junit.Test;

import java.awt.Color;
import java.util.function.Predicate;

public class LogRecordHighlighterTest {
    private LogRecordHighlighter highlighter;

    private static final Color COLOR1 = Color.RED;
    private static final Color COLOR2 = Color.BLUE;
    private static final Color COLOR3 = Color.BLACK;

    private static final ColoringFilter MATCH_FIRST_COLOR1 = makeColorFilter(TestData.MATCH_FIRST, COLOR1);
    private static final ColoringFilter MATCH_FIRST_COLOR2 = makeColorFilter(TestData.MATCH_FIRST, COLOR2);
    private static final ColoringFilter MATCH_FIRST_COLOR3 = makeColorFilter(TestData.MATCH_FIRST, COLOR3);
    private static final ColoringFilter MATCH_ALL_COLOR1 = makeColorFilter(TestData.MATCH_ALL, COLOR1);

    @Before
    public void setUp() throws Exception {
        highlighter = new LogRecordHighlighter();
    }

    @Test
    public void testDefault() throws Exception {
        assertNull(highlighter.getColor(RECORD1));
        assertNull(highlighter.getColor(RECORD2));
    }

    @Test
    public void testSimpleColor() throws Exception {
        highlighter.addFilter(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR1);
        assertEquals(COLOR1, highlighter.getColor(RECORD1));
        assertNull(highlighter.getColor(RECORD2));
    }

    @Test
    public void testOrderMatters() throws Exception {
        highlighter.addFilter(FilteringMode.HIGHLIGHT, MATCH_ALL_COLOR1);
        highlighter.addFilter(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR2);
        assertEquals(COLOR2, highlighter.getColor(RECORD1));
        assertEquals(COLOR1, highlighter.getColor(RECORD2));
    }

    @Test
    public void testRemove() throws Exception {
        highlighter.addFilter(FilteringMode.HIGHLIGHT, MATCH_ALL_COLOR1);
        highlighter.addFilter(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR2);

        highlighter.removeFilter(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR2);
        assertEquals(COLOR1, highlighter.getColor(RECORD1));
        assertEquals(COLOR1, highlighter.getColor(RECORD2));

        highlighter.removeFilter(FilteringMode.HIGHLIGHT, MATCH_ALL_COLOR1);
        assertNull(highlighter.getColor(RECORD1));
        assertNull(highlighter.getColor(RECORD2));
    }

    @Test
    public void testReplace() throws Exception {
        highlighter.addFilter(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR1);

        assertEquals(COLOR1, highlighter.getColor(RECORD1));

        highlighter.replaceFilter(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR1, MATCH_FIRST_COLOR2);

        assertEquals(COLOR2, highlighter.getColor(RECORD1));
    }

    @Test
    public void testReplaceDoesntBringToBack() throws Exception {
        highlighter.addFilter(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR2);
        highlighter.addFilter(FilteringMode.HIGHLIGHT, MATCH_ALL_COLOR1);

        assertEquals(COLOR1, highlighter.getColor(RECORD1));
        assertEquals(COLOR1, highlighter.getColor(RECORD2));

        highlighter.replaceFilter(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR2, MATCH_FIRST_COLOR3);

        assertEquals(COLOR1, highlighter.getColor(RECORD1));

        // sanity check
        highlighter.removeFilter(FilteringMode.HIGHLIGHT, MATCH_ALL_COLOR1);
        assertEquals(COLOR3, highlighter.getColor(RECORD1));
    }

    @Test
    public void testEnable() throws Exception {
        highlighter.addFilter(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR1);
        highlighter.setFilterEnabled(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR1, false);
        assertNull(highlighter.getColor(RECORD1));

        highlighter.setFilterEnabled(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR1, true);
        assertEquals(COLOR1, highlighter.getColor(RECORD1));
    }

    @Test
    public void testEnableDoesntBringToBack() throws Exception {
        highlighter.addFilter(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR2);
        highlighter.addFilter(FilteringMode.HIGHLIGHT, MATCH_ALL_COLOR1);

        assertEquals(COLOR1, highlighter.getColor(RECORD1));

        highlighter.setFilterEnabled(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR2, false);
        highlighter.setFilterEnabled(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR2, true);

        assertEquals(COLOR1, highlighter.getColor(RECORD1));
    }

    @Test
    public void testReplaceDoesntEnable() throws Exception {
        highlighter.addFilter(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR1);
        highlighter.setFilterEnabled(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR1, false);

        highlighter.replaceFilter(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR1, MATCH_FIRST_COLOR2);

        assertNull(highlighter.getColor(RECORD1));

        highlighter.setFilterEnabled(FilteringMode.HIGHLIGHT, MATCH_FIRST_COLOR2, true);

        assertEquals(COLOR2, highlighter.getColor(RECORD1));
    }

    private static ColoringFilter makeColorFilter(final Predicate<LogRecord> base, final Color color) {
        return new ColoringFilter() {
            @Override
            public Color getHighlightColor() {
                return color;
            }

            @Override
            public boolean test(@Nullable LogRecord input) {
                return base.test(input);
            }
        };
    }
}

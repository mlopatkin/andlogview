/*
 * Copyright 2016 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.search.text;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import name.mlopatkin.andlogview.search.RequestCompilationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

public class SearchRequestParserTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    SearchRequestParser.Delegate<String> mockDelegate;

    SearchRequestParser<String> parser;

    @Before
    public void setUp() throws Exception {
        lenient().when(mockDelegate.createPlainSearcher(anyString())).thenAnswer(
                (Answer<String>) invocation -> "Plain: " + invocation.getArguments()[0]);

        lenient().when(mockDelegate.createRegexpSearcher(anyString())).thenAnswer(
                (Answer<String>) invocation -> "Regexp: " + invocation.getArguments()[0]);

        parser = new SearchRequestParser<>(mockDelegate);
    }

    @Test
    public void testParse_plainText() throws Exception {
        Assert.assertEquals("Plain: Some text", parser.parse("Some text"));
    }

    @Test
    public void testParse_regex() throws Exception {
        Assert.assertEquals("Regexp: Some|text", parser.parse("/Some|text/"));
    }

    @Test
    public void testParse_regexWithSlashInMiddle() throws Exception {
        Assert.assertEquals("Regexp: Some/text", parser.parse("/Some/text/"));
    }

    @Test
    public void testParse_regexLike() throws Exception {
        Assert.assertEquals("Plain: /Some|text", parser.parse("/Some|text"));
        Assert.assertEquals("Plain: Some|text/", parser.parse("Some|text/"));
    }

    @Test(expected = RequestCompilationException.class)
    public void testParse_throwsOnEmpty() throws Exception {
        parser.parse("");
    }

    @Test(expected = RequestCompilationException.class)
    public void testParse_throwsOnEmptyRegex() throws Exception {
        parser.parse("//");
    }

    @Test(expected = RequestCompilationException.class)
    public void testParse_throwsOnBlank() throws Exception {
        parser.parse("  \t");
    }

    @Test(expected = RequestCompilationException.class)
    public void testParse_throwsOnBlankRegex() throws Exception {
        parser.parse("/  \t/");
    }
}

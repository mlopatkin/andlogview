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

import static org.hamcrest.MatcherAssert.assertThat;

import name.mlopatkin.andlogview.search.RequestCompilationException;

import com.google.common.collect.ImmutableList;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Predicate;

public class SearcherBuilderTest {
    private static class Case {
        final String text;
        final boolean needCaseConversion;
        final boolean needSubstringMatch;
        final boolean shouldMatch;

        public Case(String text, boolean needCaseConversion, boolean onlyWholeMatch, boolean shouldMatch) {
            this.text = text;
            this.needCaseConversion = needCaseConversion;
            this.needSubstringMatch = !onlyWholeMatch;
            this.shouldMatch = shouldMatch;
        }
    }

    private static final String PLAIN_PATTERN = "Hello";

    private static final ImmutableList<Case> PLAIN_CASES = ImmutableList.of(new Case("Hello", false, true, true),
            new Case("HelLo", true, true, true), new Case("hello", true, true, true),
            new Case("Say Hello", false, false, true), new Case("Hello say", false, false, true),
            new Case("He said \"Hello\" and left", false, false, true),
            new Case("He said hello and left", true, false, true), new Case("   Hello", false, false, true),
            new Case("HelloHello", false, false, true), new Case("Hello   ", false, false, true),
            new Case("Hell o", false, true, false), new Case("not a pattern", false, true, false));

    private static final String REGEXP_PATTERN = "Hel(l?)o";

    private static final ImmutableList<Case> REGEXP_CASES = ImmutableList.of(new Case("Hello", false, true, true),
            new Case("Helo", false, true, true), new Case("HelLo", true, true, true),
            new Case("HeLo", true, true, true), new Case("hello", true, true, true), new Case("helo", true, true, true),
            new Case("Say Hello", false, false, true), new Case("Say Helo", false, false, true),
            new Case("Hello say", false, false, true), new Case("Helo say", false, false, true),
            new Case("He said \"Hello\" and left", false, false, true),
            new Case("He said \"Helo\" and left", false, false, true),
            new Case("He said hello and left", true, false, true), new Case("He said helo and left", true, false, true),
            new Case("   Hello", false, false, true), new Case("   Helo", false, false, true),
            new Case("HelloHello", false, false, true), new Case("HeloHello", false, false, true),
            new Case("HeloHelo", false, false, true), new Case("Hello   ", false, false, true),
            new Case("Helo   ", false, false, true), new Case("Hell o", false, true, false),
            new Case("Hel o", false, true, false), new Case("not a pattern", false, true, false));

    @Test
    public void testBuildPlain_defaultIsMatchWholeCaseSensitive() throws Exception {
        Predicate<String> p = new SearcherBuilder().buildPlain(PLAIN_PATTERN);
        for (Case theCase : PLAIN_CASES) {
            assertThat(p.test(theCase.text), matchesCase(false, true, theCase));
        }
    }

    @Test
    public void testBuildPlain_matchWholeCaseSensitive() throws Exception {
        // Explicit specification of default
        Predicate<String> p =
                new SearcherBuilder().setIgnoreCase(false).setMatchWholeText(true).buildPlain(PLAIN_PATTERN);
        for (Case theCase : PLAIN_CASES) {
            assertThat(p.test(theCase.text), matchesCase(false, true, theCase));
        }
    }

    @Test
    public void testBuildPlain_matchWholeCaseInsensitive() throws Exception {
        Predicate<String> p =
                new SearcherBuilder().setIgnoreCase(true).setMatchWholeText(true).buildPlain(PLAIN_PATTERN);
        for (Case theCase : PLAIN_CASES) {
            assertThat(p.test(theCase.text), matchesCase(true, true, theCase));
        }
    }

    @Test
    public void testBuildPlain_matchSubstringCaseSensitive() throws Exception {
        Predicate<String> p =
                new SearcherBuilder().setIgnoreCase(false).setMatchWholeText(false).buildPlain(PLAIN_PATTERN);
        for (Case theCase : PLAIN_CASES) {
            assertThat(p.test(theCase.text), matchesCase(false, false, theCase));
        }
    }

    @Test
    public void testBuildPlain_matchSubstringCaseInsensitive() throws Exception {
        Predicate<String> p =
                new SearcherBuilder().setIgnoreCase(true).setMatchWholeText(false).buildPlain(PLAIN_PATTERN);
        for (Case theCase : PLAIN_CASES) {
            assertThat(p.test(theCase.text), matchesCase(true, false, theCase));
        }
    }

    @Test
    public void testBuildPlain_threatsRegexSyntaxVerbatim() throws Exception {
        Predicate<String> p = new SearcherBuilder().buildPlain("[abc]");

        Assert.assertFalse("Pattern shouldn't be threated as regexp", p.test("a"));
        Assert.assertTrue(p.test("[abc]"));
    }

    @Test(expected = RequestCompilationException.class)
    public void testBuildPlain_emptyPatternThrows() throws Exception {
        new SearcherBuilder().buildPlain("");
    }

    @Test
    public void testBuildRegexp_defaultIsMatchWholeCaseSensitive() throws Exception {
        Predicate<String> p = new SearcherBuilder().buildRegexp(REGEXP_PATTERN);
        for (Case theCase : REGEXP_CASES) {
            assertThat(p.test(theCase.text), matchesCase(false, true, theCase));
        }
    }

    @Test
    public void testBuildRegexp_matchWholeCaseSensitive() throws Exception {
        // Explicit specification of default
        Predicate<String> p =
                new SearcherBuilder().setIgnoreCase(false).setMatchWholeText(true).buildRegexp(REGEXP_PATTERN);

        for (Case theCase : REGEXP_CASES) {
            assertThat(p.test(theCase.text), matchesCase(false, true, theCase));
        }
    }

    @Test
    public void testBuildRegexp_matchWholeCaseInsensitive() throws Exception {
        Predicate<String> p =
                new SearcherBuilder().setIgnoreCase(true).setMatchWholeText(true).buildRegexp(REGEXP_PATTERN);

        for (Case theCase : REGEXP_CASES) {
            assertThat(p.test(theCase.text), matchesCase(true, true, theCase));
        }
    }

    @Test
    public void testBuildRegexp_matchSubstringCaseSensitive() throws Exception {
        Predicate<String> p =
                new SearcherBuilder().setIgnoreCase(false).setMatchWholeText(false).buildRegexp(REGEXP_PATTERN);

        for (Case theCase : REGEXP_CASES) {
            assertThat(p.test(theCase.text), matchesCase(false, false, theCase));
        }
    }

    @Test
    public void testBuildRegexp_matchSubstringCaseInsensitive() throws Exception {
        Predicate<String> p =
                new SearcherBuilder().setIgnoreCase(true).setMatchWholeText(false).buildRegexp(REGEXP_PATTERN);

        for (Case theCase : REGEXP_CASES) {
            assertThat(p.test(theCase.text), matchesCase(true, false, theCase));
        }
    }

    @Test(expected = RequestCompilationException.class)
    public void testBuildRegexp_emptyPatternThrows() throws Exception {
        new SearcherBuilder().buildRegexp("");
    }

    @Test(expected = RequestCompilationException.class)
    public void testBuildRegexp_invalidPatternThrows() throws Exception {
        new SearcherBuilder().buildRegexp("foo[");
    }

    private static Matcher<Boolean> matchesCase(
            final boolean performsCaseConversion, boolean matchWholeStringOnly, final Case theCase) {
        final boolean performsSubstringMatch = !matchWholeStringOnly;
        return new BaseMatcher<Boolean>() {
            @Override
            public boolean matches(Object item) {
                boolean result = (Boolean) item;
                return result == isMatchExpected();
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(theCase.text);
                if (isMatchExpected()) {
                    description.appendText(" should be matched");
                    if (theCase.needCaseConversion) {
                        description.appendText(" with case conversion");
                    }
                    if (theCase.needSubstringMatch) {
                        description.appendText(" with substring match");
                    }
                } else if (!theCase.shouldMatch) {
                    description.appendText(" should never match with any options");
                } else {
                    description.appendText(" should not match");
                    if (theCase.needCaseConversion) {
                        description.appendText(" without case conversion");
                    }
                    if (theCase.needSubstringMatch) {
                        description.appendText(" without substring match");
                    }
                }
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                boolean result = (Boolean) item;
                if (result) {
                    description.appendText("matched");
                } else {
                    description.appendText("wasn't matched");
                }
            }

            private boolean isMatchExpected() {
                // If case conversion isn't necessary but we still perform it - match should succeed anyway
                // The same is true for substring match
                boolean willCaseMatchSucceed = !theCase.needCaseConversion || performsCaseConversion;
                boolean willSubstringMatchSucceed = !theCase.needSubstringMatch || performsSubstringMatch;

                return (theCase.shouldMatch && willCaseMatchSucceed && willSubstringMatchSucceed);
            }
        };
    }
}

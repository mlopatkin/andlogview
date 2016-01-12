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
 *
 *
 */

package org.bitbucket.mlopatkin.android.logviewer.search;

import com.google.common.base.Predicate;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Utility class that helps to construct {@link Predicate} that checks string match.
 */
@ParametersAreNonnullByDefault
public class SearcherBuilder {

    private boolean matchWholeText = true;
    private boolean ignoreCase;

    /**
     * Constructs a builder object that by default will not ignore case and will attempt to match whole string.
     */
    public SearcherBuilder() {
    }

    /**
     * Sets whether resulting predicate should ignore case differences.
     *
     * @param ignoreCase if true - ignore case differences
     * @return this builder for chaining
     */
    public SearcherBuilder setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        return this;
    }

    /**
     * Sets whether resulting predicate should attempt to match pattern for the whole string. If set to false then
     * predicate will be satisfied if some substring of the argument matches pattern.
     *
     * @param matchWholeText if true - do not try to find matching substring and alway require whole text to match
     * @return this builder for chaining
     */
    public SearcherBuilder setMatchWholeText(boolean matchWholeText) {
        this.matchWholeText = matchWholeText;
        return this;
    }

    /**
     * Builds a predicate that performs simple text comparison of the pattern.
     *
     * @param pattern the plain-text pattern to use in predicate
     * @return the predicate
     * @throws RequestCompilationException if something is wrong with the pattern
     */
    public Predicate<String> buildPlain(String pattern) throws RequestCompilationException {
        return build(pattern, true);
    }

    /**
     * Builds a predicate that performs matching by threating pattern as a regular expression.
     *
     * @param pattern the regexp pattern to use in predicate
     * @return the predicate
     * @throws RequestCompilationException if something is wrong with the pattern
     */
    public Predicate<String> buildRegexp(String pattern) throws RequestCompilationException {
        return build(pattern, false);
    }

    private Predicate<String> build(String pattern, boolean isPatternLiteral) throws RequestCompilationException {
        if (pattern.isEmpty()) {
            throw new RequestCompilationException("Pattern is empty");
        }

        int flags = 0;
        if (ignoreCase) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        if (isPatternLiteral) {
            flags |= Pattern.LITERAL;
        }

        try {
            return buildRegexpSearcher(Pattern.compile(pattern, flags), matchWholeText);
        } catch (PatternSyntaxException e) {
            throw new RequestCompilationException(e);
        }
    }

    // static to avoid reference to the enclosing builder instance
    private static Predicate<String> buildRegexpSearcher(final Pattern pattern, boolean matchWholeText) {
        if (matchWholeText) {
            return new Predicate<String>() {
                @Override
                public boolean apply(@Nonnull String input) {
                    return pattern.matcher(input).matches();
                }
            };
        } else {
            return new Predicate<String>() {
                @Override
                public boolean apply(@Nonnull String input) {
                    return pattern.matcher(input).find();
                }
            };
        }
    }
}

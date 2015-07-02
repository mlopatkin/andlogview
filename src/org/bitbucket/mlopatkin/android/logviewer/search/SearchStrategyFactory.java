/*
 * Copyright 2011 Mikhail Lopatkin
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
package org.bitbucket.mlopatkin.android.logviewer.search;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Creates {@link SearchStrategy}s and {@link HighlightStrategy}s according to
 * the specified request string.
 * <p>
 * The request string must obey following rules:
 * <ol>
 * <li>If the string starts and ends with '/' (slash) then the method returns
 * regex-based searcher/highlighter. All between slashes is treated as the
 * regular expression.
 * <li>If the string doesn't fall into any of the categories above then the
 * method returns searcher/highlighter that searches for a verbatim occurence of
 * the pattern (case-insensitive)
 *
 * </ol>
 *
 * @see Pattern
 */
public class SearchStrategyFactory {

    // this is static-only class
    private SearchStrategyFactory() {
    }

    private static final char REGEX_BOUND_CHAR = '/';

    private static boolean isRegexRequest(String request) {
        assert request != null;
        final int length = request.length();
        if (length > 1) {
            if (request.charAt(0) == REGEX_BOUND_CHAR
                    && request.charAt(length - 1) == REGEX_BOUND_CHAR) {
                return true;
            }
        }
        return false;
    }

    private static String extractRegexRequest(String request) {
        assert isRegexRequest(request) : request + " is not a regex";
        return request.substring(1, request.length() - 1);
    }

    public static HighlightStrategy createHighlightStrategy(String request)
            throws RequestCompilationException {
        if (!CharMatcher.WHITESPACE.matchesAllOf(Strings.nullToEmpty(request))) {
            if (isRegexRequest(request)) {
                try {
                    String regexRequest = extractRegexRequest(request);
                    if (!CharMatcher.WHITESPACE.matchesAllOf(regexRequest)) {
                        return new RegExpSearcher(regexRequest);
                    } else {
                        throw new RequestCompilationException(request + " contains blank regex");
                    }
                } catch (PatternSyntaxException e) {
                    throw new RequestCompilationException(e.getMessage(), e);
                }
            } else {
                return new IgnoreCaseSearcher(request);
            }
        } else {
            return null;
        }
    }

    public static SearchStrategy createSearchStrategy(String request)
            throws RequestCompilationException {
        return createHighlightStrategy(request);
    }
}

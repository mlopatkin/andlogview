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
package name.mlopatkin.andlogview.search;

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
    private static class DelegateImpl implements SearchRequestParser.Delegate<HighlightStrategy> {
        @Override
        public HighlightStrategy createRegexpSearcher(String pattern) throws RequestCompilationException {
            try {
                return new RegExpSearcher(pattern);
            } catch (PatternSyntaxException e) {
                throw new RequestCompilationException(e.getDescription(), pattern, e);
            }
        }

        @Override
        public HighlightStrategy createPlainSearcher(String pattern) throws RequestCompilationException {
            return new IgnoreCaseSearcher(pattern);
        }
    }

    private static final SearchRequestParser<HighlightStrategy> requestParser =
            new SearchRequestParser<>(new DelegateImpl());

    // this is static-only class
    private SearchStrategyFactory() {}

    public static HighlightStrategy createHighlightStrategy(String request) throws RequestCompilationException {
        return requestParser.parse(request);
    }
}

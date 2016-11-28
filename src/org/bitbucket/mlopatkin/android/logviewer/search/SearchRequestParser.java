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

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * This class parses a single query and determines whether it is a plain text search or regex. Then it extracts query
 * value and asks delegate to create a searcher.
 * The request string must obey following rules:
 * <ol>
 * <li>If the string starts and ends with '/' (slash) then all between slashes is treated as the
 * regular expression.
 * <li>otherwise it is threated as a plain-text pattern.
 * </ol>
 *
 * @param <T> the type of the searcher to build
 */
@ParametersAreNonnullByDefault
public class SearchRequestParser<T> {
    /**
     * Delegate that actually creates a searcher.
     *
     * @param <T> the type of the searcher
     */
    public interface Delegate<T> {
        /**
         * Create a searcher for a regex pattern. No further processing of the pattern string is needed.
         *
         * @param pattern the regular expression pattern
         * @return the searcher
         * @throws RequestCompilationException if the pattern isn't valid
         */
        T createRegexpSearcher(String pattern) throws RequestCompilationException;

        /**
         * Create a searcher for a plain text pattern. No further processing of the pattern string is needed.
         *
         * @param pattern the plain text pattern
         * @return the searcher
         * @throws RequestCompilationException if the pattern isn't valid
         */
        T createPlainSearcher(String pattern) throws RequestCompilationException;
    }

    private static final char REGEX_BOUND_CHAR = '/';

    private final Delegate<T> delegate;

    public SearchRequestParser(Delegate<T> delegate) {
        this.delegate = delegate;
    }

    private boolean isRegexRequest(String request) {
        final int length = request.length();
        if (length > 1) {
            if (request.charAt(0) == REGEX_BOUND_CHAR
                    && request.charAt(length - 1) == REGEX_BOUND_CHAR) {
                return true;
            }
        }
        return false;
    }

    private String extractRegexRequest(String request) {
        assert isRegexRequest(request) : request + " is not a regex";
        return request.substring(1, request.length() - 1);
    }

    /**
     * Creates a searcher from a request.
     * <p/>
     * See class Javadoc for request syntax details.
     *
     * @param request the request
     * @return the new searcher
     * @throws RequestCompilationException if the request cannot be parsed
     */
    @Nonnull
    public T parse(String request) throws RequestCompilationException {
        Preconditions.checkNotNull(request);

        if (CharMatcher.whitespace().matchesAllOf(request)) {
            throw new RequestCompilationException("Input string for searching is empty or blank", request);
        }
        if (isRegexRequest(request)) {
            String regexRequest = extractRegexRequest(request);
            // TODO what if I really want to find two spaces in a log?
            if (CharMatcher.whitespace().matchesAllOf(regexRequest)) {
                throw new RequestCompilationException("Blank regex", request);
            }
            try {
                return delegate.createRegexpSearcher(regexRequest);
            } catch (RequestCompilationException e) {
                // reset with original request string and rethrow
                e.setRequestValue(request);
                throw e;
            }
        }
        return delegate.createPlainSearcher(request);

    }
}

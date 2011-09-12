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
package org.bitbucket.mlopatkin.android.liblogcat.filters;

import java.util.regex.PatternSyntaxException;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.search.SearchStrategy;
import org.bitbucket.mlopatkin.android.logviewer.search.SearchStrategyFactory;

/**
 * Performs filtering based on the message of the record.
 */
public class MessageFilter extends AbstractFilter implements LogRecordFilter {

    private SearchStrategy strategy;
    private String requestText;

    public MessageFilter(String request) throws PatternSyntaxException {
        this.requestText = request;
        strategy = SearchStrategyFactory.createSearchStrategy(request);
    }

    @Override
    public boolean include(LogRecord record) {
        String message = record.getMessage();
        return strategy.isStringMatched(message);
    }

    @Override
    public String toString() {
        return "Message containing '" + requestText + "'";
    }

    @Override
    protected void dumpFilter(FilterData data) {
        data.message = requestText;
    }

}

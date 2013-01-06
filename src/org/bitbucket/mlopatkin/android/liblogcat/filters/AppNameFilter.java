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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.search.SearchStrategy;
import org.bitbucket.mlopatkin.android.logviewer.search.SearchStrategyFactory;

/**
 * Performs filtering based on the message of the record.
 */
public class AppNameFilter extends AbstractFilter implements LogRecordFilter {

    private List<SearchStrategy> strategies = new ArrayList<SearchStrategy>();
    private List<String> requestTexts = new ArrayList<String>();

    public AppNameFilter(List<String> appNames) throws PatternSyntaxException {
        for (String request : appNames) {
            requestTexts.add(request);
            strategies.add(SearchStrategyFactory.createSearchStrategy(request));
        }
    }

    @Override
    public boolean include(LogRecord record) {
        String appName = record.getAppName();
        for(SearchStrategy strategy : strategies) {
            if(strategy.isStringMatched(appName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Application name like " + requestTexts;
    }

    @Override
    protected void dumpFilter(FilterData data) {
        data.appNames = requestTexts;
    }

    public static AppNameFilter fromList(List<String> appNames) {
        if (appNames == null || appNames.isEmpty()) {
            return null;
        } else {
            return new AppNameFilter(appNames);
        }
    }
}

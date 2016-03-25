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

import com.google.common.base.Predicate;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;
import org.bitbucket.mlopatkin.android.logviewer.search.SearchStrategy;
import org.bitbucket.mlopatkin.android.logviewer.search.SearchStrategyFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs filtering based on the message of the record.
 */
public class AppNameFilter implements Predicate<LogRecord> {

    private final List<SearchStrategy> strategies = new ArrayList<>();
    private final List<String> requestTexts = new ArrayList<>();

    public AppNameFilter(List<String> appNames) throws RequestCompilationException {
        for (String request : appNames) {
            requestTexts.add(request);
            strategies.add(SearchStrategyFactory.createSearchStrategy(request));
        }
    }

    @Override
    public boolean apply(LogRecord record) {
        String appName = record.getAppName();
        for (SearchStrategy strategy : strategies) {
            if (strategy.apply(appName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Application name like " + requestTexts;
    }

}

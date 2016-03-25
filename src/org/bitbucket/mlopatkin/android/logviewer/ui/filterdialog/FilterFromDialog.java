/*
 * Copyright 2014 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordPredicates;
import org.bitbucket.mlopatkin.android.liblogcat.filters.AppNameFilter;
import org.bitbucket.mlopatkin.android.logviewer.filters.ColoringFilter;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilteringMode;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;
import org.bitbucket.mlopatkin.android.logviewer.search.SearchRequestParser;
import org.bitbucket.mlopatkin.android.logviewer.search.SearchStrategyFactory;
import org.bitbucket.mlopatkin.android.logviewer.search.SearcherBuilder;

import java.awt.Color;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FilterFromDialog implements ColoringFilter {

    private static final Joiner commaJoiner = Joiner.on(", ");

    private static final SearchRequestParser.Delegate<Predicate<String>> tagParserDelegate =
            new SearchRequestParser.Delegate<Predicate<String>>() {
                private SearcherBuilder matchWhole =
                        new SearcherBuilder().setIgnoreCase(true).setMatchWholeText(true);
                private SearcherBuilder matchSubstring =
                        new SearcherBuilder().setIgnoreCase(true).setMatchWholeText(false);

                @Override
                public Predicate<String> createRegexpSearcher(@Nonnull String pattern)
                        throws RequestCompilationException {
                    return matchSubstring.buildRegexp(pattern);
                }

                @Override
                public Predicate<String> createPlainSearcher(@Nonnull String pattern)
                        throws RequestCompilationException {
                    return matchWhole.buildPlain(pattern);
                }
            };

    private static final SearchRequestParser<Predicate<String>> tagParser =
            new SearchRequestParser<>(tagParserDelegate);

    private List<String> tags;
    private List<Integer> pids;
    private List<String> apps;
    private String messagePattern;

    private LogRecord.Priority priority;
    private FilteringMode mode;

    private Color highlightColor;

    private transient Predicate<LogRecord> compiledPredicate;
    private transient String tooltipRepresentation;

    public FilterFromDialog() {
    }

    public void initialize() throws RequestCompilationException {
        assert compiledPredicate == null;
        assert tooltipRepresentation == null;
        compiledPredicate = compilePredicate();
        tooltipRepresentation = compileTooltip();
    }

    @Override
    public boolean apply(@Nullable LogRecord input) {
        return compiledPredicate.apply(input);
    }

    private Predicate<LogRecord> compilePredicate() throws RequestCompilationException {
        List<Predicate<LogRecord>> predicates = Lists.newArrayListWithCapacity(4);
        if (tags != null && !tags.isEmpty()) {
            List<Predicate<String>> tagPredicates = Lists.newArrayListWithCapacity(tags.size());
            for (String tagPattern : tags) {
                tagPredicates.add(tagParser.parse(tagPattern));
            }
            predicates.add(LogRecordPredicates.matchTag(Predicates.or(tagPredicates)));
        }
        Predicate<LogRecord> appsAndPidsPredicate = null;
        if (pids != null && !pids.isEmpty()) {
            appsAndPidsPredicate = LogRecordPredicates.withAnyOfPids(pids);
        }
        if (apps != null && !apps.isEmpty()) {
            AppNameFilter appNameFilter = new AppNameFilter(apps);
            appsAndPidsPredicate =
                    appsAndPidsPredicate != null ? Predicates.or(appsAndPidsPredicate, appNameFilter) : appNameFilter;
        }
        if (appsAndPidsPredicate != null) {
            predicates.add(appsAndPidsPredicate);
        }
        if (messagePattern != null && !messagePattern.isEmpty()) {
            predicates.add(LogRecordPredicates
                    .matchMessage(SearchStrategyFactory.createSearchStrategy(messagePattern)));
        }
        if (priority != null && priority != LogRecord.Priority.LOWEST) {
            predicates.add(LogRecordPredicates.moreSevereThan(priority));
        }
        return Predicates.and(predicates);
    }

    private String compileTooltip() {
        StringBuilder builder = new StringBuilder("<html>");
        builder.append(mode.getDescription());
        if (tags != null && !tags.isEmpty()) {
            builder.append("<br>Tags: ");
            commaJoiner.appendTo(builder, tags);
        }
        if (pids != null && !pids.isEmpty()) {
            builder.append("<br>PIDs: ");
            commaJoiner.appendTo(builder, pids);
        }
        if (apps != null && !apps.isEmpty()) {
            builder.append("<br>App names like: ");
            commaJoiner.appendTo(builder, apps);
        }
        if (messagePattern != null && !messagePattern.isEmpty()) {
            builder.append("<br>Message text like: ").append(messagePattern);
        }
        if (priority != null && priority != LogRecord.Priority.LOWEST) {
            builder.append("<br>Priority>=").append(priority);
        }
        return builder.append("</html>").toString();
    }

    public List<String> getTags() {
        return tags;
    }

    public FilterFromDialog setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public List<Integer> getPids() {
        return pids;
    }

    public FilterFromDialog setPids(List<Integer> pids) {
        this.pids = pids;
        return this;
    }

    public List<String> getApps() {
        return apps;
    }

    public FilterFromDialog setApps(List<String> apps) {
        this.apps = apps;
        return this;
    }

    public String getMessagePattern() {
        return messagePattern;
    }

    public FilterFromDialog setMessagePattern(String messagePattern) {
        this.messagePattern = messagePattern;
        return this;
    }

    public LogRecord.Priority getPriority() {
        return priority;
    }

    public void setPriority(LogRecord.Priority priority) {
        this.priority = priority;
    }

    public FilteringMode getMode() {
        return mode;
    }

    public void setMode(FilteringMode mode) {
        this.mode = mode;
    }

    @Override
    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    public String getTooltip() {
        return tooltipRepresentation;
    }
}

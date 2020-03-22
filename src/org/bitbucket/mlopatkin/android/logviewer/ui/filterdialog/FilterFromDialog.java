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
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordPredicates;
import org.bitbucket.mlopatkin.android.logviewer.filters.ColoringFilter;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilteringMode;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;
import org.bitbucket.mlopatkin.android.logviewer.search.SearchRequestParser;
import org.bitbucket.mlopatkin.android.logviewer.search.SearcherBuilder;
import org.bitbucket.mlopatkin.utils.MorePredicates;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

public class FilterFromDialog implements ColoringFilter {
    private static final Joiner commaJoiner = Joiner.on(", ");

    private static final SearchRequestParser<Predicate<String>> tagParser =
            new SearchRequestParser<>(new SearchRequestParser.Delegate<Predicate<String>>() {
                private SearcherBuilder matchIgnoreCase = new SearcherBuilder().setIgnoreCase(true);

                @Override
                public Predicate<String> createRegexpSearcher(@Nonnull String pattern)
                        throws RequestCompilationException {
                    return matchIgnoreCase.setMatchWholeText(false).buildRegexp(pattern);
                }

                @Override
                public Predicate<String> createPlainSearcher(@Nonnull String pattern)
                        throws RequestCompilationException {
                    return matchIgnoreCase.setMatchWholeText(true).buildPlain(pattern);
                }
            });

    private static final SearchRequestParser<Predicate<String>> messageParser =
            new SearchRequestParser<>(new SearchRequestParser.Delegate<Predicate<String>>() {
                private final SearcherBuilder matchSubstring = new SearcherBuilder().setMatchWholeText(false);

                @Override
                public Predicate<String> createRegexpSearcher(String pattern) throws RequestCompilationException {
                    return matchSubstring.setIgnoreCase(false).buildRegexp(pattern);
                }

                @Override
                public Predicate<String> createPlainSearcher(String pattern) throws RequestCompilationException {
                    return matchSubstring.setIgnoreCase(true).buildPlain(pattern);
                }
            });

    private List<String> tags;
    private List<Integer> pids;
    private List<String> apps;
    private String messagePattern;

    private LogRecord.Priority priority;
    private FilteringMode mode;

    private Color highlightColor;

    private transient Predicate<LogRecord> compiledPredicate;
    private transient String tooltipRepresentation;

    public FilterFromDialog() {}

    public void initialize() throws RequestCompilationException {
        assert compiledPredicate == null;
        assert tooltipRepresentation == null;
        assert mode != null;
        compiledPredicate = compilePredicate();
        tooltipRepresentation = compileTooltip();
    }

    @Override
    public boolean test(LogRecord input) {
        return compiledPredicate.test(input);
    }

    private Predicate<LogRecord> compilePredicate() throws RequestCompilationException {
        List<Predicate<LogRecord>> predicates = Lists.newArrayListWithCapacity(4);
        if (tags != null && !tags.isEmpty()) {
            List<Predicate<String>> tagPredicates = Lists.newArrayListWithCapacity(tags.size());
            for (String tagPattern : tags) {
                tagPredicates.add(tagParser.parse(tagPattern));
            }
            predicates.add(LogRecordPredicates.matchTag(MorePredicates.or(tagPredicates)));
        }
        Predicate<LogRecord> appsAndPidsPredicate = null;
        if (pids != null && !pids.isEmpty()) {
            appsAndPidsPredicate = LogRecordPredicates.withAnyOfPids(pids);
        }
        if (apps != null && !apps.isEmpty()) {
            List<Predicate<String>> appsPredicates = Lists.newArrayListWithCapacity(apps.size());
            for (String appPattern : apps) {
                appsPredicates.add(tagParser.parse(appPattern));
            }
            Predicate<LogRecord> appsPredicate = LogRecordPredicates.matchAppName(MorePredicates.or(appsPredicates));
            appsAndPidsPredicate =
                    appsAndPidsPredicate != null ? appsPredicate.or(appsAndPidsPredicate) : appsPredicate;
        }
        if (appsAndPidsPredicate != null) {
            predicates.add(appsAndPidsPredicate);
        }
        if (messagePattern != null && !messagePattern.isEmpty()) {
            predicates.add(LogRecordPredicates.matchMessage(messageParser.parse(messagePattern)));
        }
        if (priority != null && priority != LogRecord.Priority.LOWEST) {
            predicates.add(LogRecordPredicates.moreSevereThan(priority));
        }
        return MorePredicates.and(predicates);
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

    public FilterFromDialog setPriority(LogRecord.Priority priority) {
        this.priority = priority;
        return this;
    }

    public FilteringMode getMode() {
        return mode;
    }

    public FilterFromDialog setMode(FilteringMode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public Color getHighlightColor() {
        return highlightColor;
    }

    public FilterFromDialog setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
        return this;
    }

    public String getTooltip() {
        return tooltipRepresentation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FilterFromDialog that = (FilterFromDialog) o;
        return Objects.equals(tags, that.tags)
                && Objects.equals(pids, that.pids)
                && Objects.equals(apps, that.apps)
                && Objects.equals(messagePattern, that.messagePattern)
                && priority == that.priority
                && mode == that.mode
                && Objects.equals(highlightColor, that.highlightColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tags, pids, apps, messagePattern, priority, mode, highlightColor);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("mode", mode)
                .add("tags", tags)
                .add("messagePattern", messagePattern)
                .add("pids", pids)
                .add("apps", apps)
                .add("priority", priority)
                .add("highlightColor", highlightColor)
                .toString();
    }
}

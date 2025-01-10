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

package name.mlopatkin.andlogview.ui.filterdialog;

import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecordPredicates;
import name.mlopatkin.andlogview.search.RequestCompilationException;
import name.mlopatkin.andlogview.search.text.SearchRequestParser;
import name.mlopatkin.andlogview.search.text.SearcherBuilder;
import name.mlopatkin.andlogview.utils.MorePredicates;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class FilterFromDialogData {
    private static final Joiner commaJoiner = Joiner.on(", ");

    private static final SearchRequestParser<Predicate<String>> tagParser =
            new SearchRequestParser<>(new SearchRequestParser.Delegate<>() {
                private final SearcherBuilder matchIgnoreCase = new SearcherBuilder().setIgnoreCase(true);

                @Override
                public Predicate<String> createRegexpSearcher(String pattern)
                        throws RequestCompilationException {
                    return matchIgnoreCase.setMatchWholeText(false).buildRegexp(pattern);
                }

                @Override
                public Predicate<String> createPlainSearcher(String pattern)
                        throws RequestCompilationException {
                    return matchIgnoreCase.setMatchWholeText(true).buildPlain(pattern);
                }
            });

    private static final SearchRequestParser<Predicate<String>> messageParser =
            new SearchRequestParser<>(new SearchRequestParser.Delegate<>() {
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

    private final FilteringMode mode;

    private @Nullable String name;
    private List<String> tags = ImmutableList.of();
    private List<Integer> pids = ImmutableList.of();
    private List<String> apps = ImmutableList.of();
    private @Nullable String messagePattern;
    private LogRecord.@Nullable Priority priority;
    private @Nullable Color highlightColor;

    public FilterFromDialogData(FilteringMode mode) {
        this.mode = mode;
    }

    public FilterFromDialogData(FilterFromDialogData f) {
        mode = f.mode;
        tags = ImmutableList.copyOf(f.tags);
        pids = ImmutableList.copyOf(f.pids);
        apps = ImmutableList.copyOf(f.apps);
        messagePattern = f.messagePattern;
        priority = f.priority;
        highlightColor = f.highlightColor;
        name = f.name;
    }

    Predicate<LogRecord> compilePredicate() throws RequestCompilationException {
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

    public String getTooltip() {
        StringBuilder builder = new StringBuilder("<html>");
        if (name != null) {
            builder.append(name).append("<br>");
        }
        assert mode != null;
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

    public FilterFromDialogData setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public List<Integer> getPids() {
        return pids;
    }

    public FilterFromDialogData setPids(List<Integer> pids) {
        this.pids = pids;
        return this;
    }

    public List<String> getApps() {
        return apps;
    }

    public FilterFromDialogData setApps(List<String> apps) {
        this.apps = apps;
        return this;
    }

    public @Nullable String getMessagePattern() {
        return messagePattern;
    }

    public FilterFromDialogData setMessagePattern(@Nullable String messagePattern) {
        this.messagePattern = messagePattern;
        return this;
    }

    public LogRecord.@Nullable Priority getPriority() {
        return priority;
    }

    public FilterFromDialogData setPriority(LogRecord.@Nullable Priority priority) {
        this.priority = priority;
        return this;
    }

    public FilteringMode getMode() {
        return mode;
    }

    public @Nullable Color getHighlightColor() {
        return highlightColor;
    }

    public FilterFromDialogData setHighlightColor(@Nullable Color highlightColor) {
        this.highlightColor = highlightColor;
        return this;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FilterFromDialogData that)) {
            return false;
        }
        return Objects.equals(tags, that.tags)
               && Objects.equals(pids, that.pids)
               && Objects.equals(apps, that.apps)
               && Objects.equals(messagePattern, that.messagePattern)
               && priority == that.priority
               && mode == that.mode
               && Objects.equals(highlightColor, that.highlightColor)
               && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tags, pids, apps, messagePattern, priority, mode, highlightColor, name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("mode", mode)
                .add("tags", tags)
                .add("messagePattern", messagePattern)
                .add("pids", pids)
                .add("apps", apps)
                .add("priority", priority)
                .add("highlightColor", highlightColor)
                .toString();
    }

    public FilterFromDialog toFilter(boolean enabled) throws RequestCompilationException {
        if (mode == FilteringMode.WINDOW) {
            return new IndexWindowFilter(enabled, new FilterFromDialogData(this));
        }
        return new FilterFromDialogImpl(enabled, new FilterFromDialogData(this));
    }

    public FilterFromDialog toFilter() throws RequestCompilationException {
        return toFilter(true);
    }

    public FilterFromDialogData setName(@Nullable String name) {
        this.name = name;
        return this;
    }

    public @Nullable String getName() {
        return name;
    }
}

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

package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.Color;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordPredicates;
import org.bitbucket.mlopatkin.android.liblogcat.filters.AppNameFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.MultiTagFilter;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilterStorage;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;
import org.bitbucket.mlopatkin.android.logviewer.search.SearchStrategyFactory;

public class FilterFromDialog implements Predicate<LogRecord> {

    private static final Joiner commaJoiner = Joiner.on(", ");
    public static final FilterStorage.FilterStorageClient<List<FilterFromDialog>> STORAGE_CLIENT =
            new FilterFromDialogStorageClient();

    private List<String> tags;
    private List<Integer> pids;
    private List<String> apps;
    private String messagePattern;

    private LogRecord.Priority priority;
    private FilteringMode mode;

    private Color highlightColor;

    private transient Predicate<LogRecord> compiledPredicate;
    private transient String tooltipRepresentation;

    // for JSON deserialization
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
        List<Predicate<LogRecord>> predicates = Lists.newArrayListWithCapacity(6);
        if (tags != null && !tags.isEmpty()) {
            predicates.add(new MultiTagFilter(tags.toArray(new String[tags.size()])));
        }
        if (pids != null && !pids.isEmpty()) {
            predicates.add(LogRecordPredicates.withAnyOfPids(pids));
        }
        if (apps != null && !apps.isEmpty()) {
            predicates.add(new AppNameFilter(apps));
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

    private static class FilterFromDialogStorageClient
            implements FilterStorage.FilterStorageClient<List<FilterFromDialog>> {

        private static final Type FILTER_LIST_TYPE =
                new TypeToken<List<FilterFromDialog>>() {}.getType();

        FilterFromDialogStorageClient() {
        }

        @Override
        public String getName() {
            return "dialog_filters";
        }

        @Override
        public List<FilterFromDialog> fromJson(Gson gson, JsonElement element) {
            List<FilterFromDialog> filters = gson.fromJson(element, FILTER_LIST_TYPE);
            for (FilterFromDialog filter : filters) {
                try {
                    filter.initialize();
                } catch (RequestCompilationException e) {
                    throw new JsonSyntaxException("Can't compile filters", e);
                }
            }
            return filters;
        }

        @Override
        public List<FilterFromDialog> getDefault() {
            return Collections.emptyList();
        }

        @Override
        public JsonElement toJson(Gson gson, List<FilterFromDialog> value) {
            return gson.toJsonTree(value, FILTER_LIST_TYPE);
        }
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Integer> getPids() {
        return pids;
    }

    public void setPids(List<Integer> pids) {
        this.pids = pids;
    }

    public List<String> getApps() {
        return apps;
    }

    public void setApps(List<String> apps) {
        this.apps = apps;
    }

    public String getMessagePattern() {
        return messagePattern;
    }

    public void setMessagePattern(String messagePattern) {
        this.messagePattern = messagePattern;
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

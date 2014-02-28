/*
 * Copyright 2013, 2014 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.filters;

import static org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordPredicates;
import org.bitbucket.mlopatkin.android.logviewer.FilteringMode;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;
import org.bitbucket.mlopatkin.android.logviewer.search.SearchStrategy;
import org.bitbucket.mlopatkin.android.logviewer.search.SearchStrategyFactory;
import org.bitbucket.mlopatkin.utils.FluentPredicate;
import org.bitbucket.mlopatkin.utils.JsonUtils;

public class FilterDialogData {

    private FilteringMode mode = FilteringMode.getDefaultMode();
    private Color highlightColor = null;

    private List<String> tags = Collections.emptyList();
    private List<String> applications = Collections.emptyList();
    private List<Integer> pids = Collections.emptyList();
    private String message = null;
    private Priority selectedPriority = null;

    public FilterDialogData() {
    }

    public FilterDialogData(JSONObject json) throws JSONException {
        mode = FilteringMode.valueOf(json.getString("mode"));
        highlightColor = stringToColor(json.optString("highlight"));
        tags = Lists.newArrayList(JsonUtils.asStringIterable(json.getJSONArray("tags")));
        applications = Lists
                .newArrayList(JsonUtils.asStringIterable(json.getJSONArray("applications")));
        pids = Lists.newArrayList(JsonUtils.asIntIterable(json.getJSONArray("pids")));
        message = json.optString("message", null);
        selectedPriority = safeFromString(json.optString("selectedPriority"));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj.getClass() != getClass()) {
            return false;
        }

        FilterDialogData other = (FilterDialogData) obj;

        return Objects.equal(mode, other.mode)
                && Objects.equal(highlightColor, other.highlightColor)
                && Objects.equal(tags, other.tags)
                && Objects.equal(applications, other.applications)
                && Objects.equal(pids, other.pids)
                && Objects.equal(message, other.message)
                && Objects.equal(selectedPriority, other.selectedPriority);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mode, highlightColor, tags, applications, pids, message,
                selectedPriority);
    }


    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("mode", mode)
                .add("highlight", highlightColor)
                .add("tags", tags)
                .add("apps", applications)
                .add("pids", pids)
                .add("msg", message)
                .add("priority", selectedPriority)
                .toString();
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getApplications() {
        return applications;
    }

    public void setApplications(List<String> applications) {
        this.applications = applications;
    }

    public List<Integer> getPids() {
        return pids;
    }

    public void setPids(List<Integer> pids) {
        this.pids = pids;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Priority getSelectedPriority() {
        return selectedPriority;
    }

    public void setSelectedPriority(Priority selectedPriority) {
        this.selectedPriority = selectedPriority;
    }

    public LogRecordFilter makeFilter() {
        FluentPredicate<LogRecord> predicate = FluentPredicate.alwaysTrue();
        predicate = predicate.and(makeFilterForAppNamesAndPids());
        predicate = predicate.and(makeTagsFilter());
        predicate = predicate.and(makeMessageFilter());
        predicate = predicate.and(makePriorityFilter());
        return new LogRecordFilter(predicate);
    }

    @Nullable
    private FluentPredicate<LogRecord> makeFilterForAppNames() {
        if (applications == null || applications.isEmpty()) {
            return null;
        }
        Predicate<String> appNamePredicate = makeCompoundStringSearchPattern(getApplications());
        return LogRecordPredicates.matchAppName(appNamePredicate);
    }

    @Nullable
    private FluentPredicate<LogRecord> makeFilterForPids() {
        if (pids != null && !pids.isEmpty()) {
            return LogRecordPredicates.withAnyOfPids(pids);
        }
        return null;
    }

    @Nullable
    private FluentPredicate<LogRecord> makeFilterForAppNamesAndPids() {
        FluentPredicate<LogRecord> appNames = makeFilterForAppNames();
        FluentPredicate<LogRecord> pids = makeFilterForPids();

        return appNames != null ? appNames.or(pids) : pids;
    }

    @Nullable
    private FluentPredicate<LogRecord> makeTagsFilter() {
        if (tags != null && !tags.isEmpty()) {
            return LogRecordPredicates.matchTag(Predicates.in(Sets.newHashSet(tags)));
        }
        return null;
    }

    @Nullable
    private FluentPredicate<LogRecord> makeMessageFilter() {
        if (!Strings.isNullOrEmpty(message)) {
            return LogRecordPredicates.matchMessage(makeStringSearchPredicate(message));
        }
        return null;
    }

    @Nullable
    private FluentPredicate<LogRecord> makePriorityFilter() {
        if (selectedPriority != null && selectedPriority != Priority.LOWEST) {
            return LogRecordPredicates.moreSevereThan(selectedPriority);
        }
        return null;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("mode", mode.toString());
        result.putOpt("highlight", colorToString(highlightColor));
        result.put("applications", new JSONArray(applications));
        result.put("pids", new JSONArray(pids));
        result.put("tags", new JSONArray(tags));
        result.putOpt("message", message);
        result.putOpt("selectedPriority", safeToString(selectedPriority));
        return result;
    }

    private static String colorToString(Color color) {
        if (color == null) {
            return null;
        }
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private static Color stringToColor(String str) {
        str = Strings.emptyToNull(str);
        return str != null ? Color.decode(str) : null;
    }

    private static <T> String safeToString(T object) {
        return object != null ? object.toString() : null;
    }

    private static Priority safeFromString(String value) {
        return value != null ? Priority.valueOf(value) : null;
    }

    private static Predicate<String> makeStringSearchPredicate(String pattern) {
        try {
            final SearchStrategy searchStrategy = SearchStrategyFactory
                    .createSearchStrategy(pattern);
            return new FluentPredicate<String>() {
                @Override
                public boolean apply(@Nullable String input) {
                    return searchStrategy.isStringMatched(input);
                }
            };
        } catch (RequestCompilationException e) {
            // request must be validated
            throw new AssertionError(e);
        }
    }


    @Nullable
    private static Predicate<String> makeCompoundStringSearchPattern(List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return null;
        }
        List<Predicate<String>> patternPredicates = Lists.newArrayListWithCapacity(patterns.size());
        for (String pattern : patterns) {
            patternPredicates.add(makeStringSearchPredicate(pattern));
        }
        return Predicates.or(patternPredicates);
    }
}

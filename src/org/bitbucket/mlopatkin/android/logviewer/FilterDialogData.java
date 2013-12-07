/*
 * Copyright 2013 Mikhail Lopatkin
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

import static org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Strings;

import org.bitbucket.mlopatkin.android.liblogcat.filters.AppNameFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.AppNameOrPidFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.LogRecordFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.MessageFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.MultiPidFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.MultiTagFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.NullFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.PriorityFilter;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;

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
        LogRecordFilter filter = NullFilter.INSTANCE;
        filter = filter.and(makeFilterForAppNamesAndPids());
        filter = filter.and(makeTagsFilter());
        filter = filter.and(makeMessageFilter());
        filter = filter.and(makePriorityFilter());
        return filter;
    }

    private LogRecordFilter makeFilterForAppNamesAndPids() {
        try {
            return AppNameOrPidFilter
                    .or(AppNameFilter.fromList(applications),
                            MultiPidFilter.fromList(pids));
        } catch (RequestCompilationException e) {
            // request must be validated
            throw new AssertionError(e);
        }
    }

    private LogRecordFilter makeTagsFilter() {
        if (!tags.isEmpty()) {
            return new MultiTagFilter(tags.toArray(new String[tags.size()]));
        }
        return null;
    }

    private LogRecordFilter makeMessageFilter() {
        if (!Strings.isNullOrEmpty(message)) {
            try {
                return new MessageFilter(message);
            } catch (RequestCompilationException e) {
                // request must be validated
                throw new AssertionError(e);
            }
        }
        return null;
    }

    private LogRecordFilter makePriorityFilter() {
        if (selectedPriority != null) {
            return new PriorityFilter(selectedPriority);
        }
        return null;
    }
}

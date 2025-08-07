/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.filters;

import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.logmodel.LogRecord;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.html.HtmlEscapers;

import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Constructs a filter description suitable for tooltip or tree display.
 */
class FilterDescriptionBuilder {
    private static final Joiner commaJoiner = Joiner.on(", ");

    private final StringBuilder content;
    private final Deque<String> tags = new ArrayDeque<>();

    private boolean isFirstLine = true;

    public FilterDescriptionBuilder() {
        content = new StringBuilder("<html>");
        tags.push("html");
    }

    private FilterDescriptionBuilder beginLine() {
        if (!isFirstLine) {
            tag("br");
        }
        return this;
    }

    private FilterDescriptionBuilder endLine() {
        isFirstLine = false;
        return this;
    }

    public FilterDescriptionBuilder addMode(FilteringMode mode) {
        assert !isFinished();

        return beginLine().append(mode.getDescription()).endLine();
    }

    public FilterDescriptionBuilder addName(@Nullable String name) {
        assert !isFinished();

        if (name != null) {
            beginLine().startTag("b").append(name).endTag().endLine();
        }

        return this;
    }

    public FilterDescriptionBuilder addList(String caption, @Nullable List<?> elements) {
        assert !isFinished();

        if (elements != null && !elements.isEmpty()) {
            beginLine().append(caption).append(": ");
            commaJoiner.appendTo(content, Iterables.transform(elements, FilterDescriptionBuilder::escape));
        }
        return this;
    }

    public FilterDescriptionBuilder addPattern(String caption, @Nullable String messagePattern) {
        assert !isFinished();

        if (messagePattern != null && !messagePattern.isEmpty()) {
            beginLine().append(caption).append(": ").append(messagePattern).endLine();
        }
        return this;
    }

    public FilterDescriptionBuilder addPriorityBound(String caption, LogRecord.@Nullable Priority priority) {
        assert !isFinished();

        if (priority != null && priority != LogRecord.Priority.LOWEST) {
            beginLine().append(caption).append(priority).endLine();
        }
        return this;
    }

    public String build() {
        if (!isFinished()) {
            endTag();
        }
        return content.toString();
    }

    private FilterDescriptionBuilder append(Object rawText) {
        content.append(escape(rawText));
        return this;
    }

    private static String escape(Object rawText) {
        return HtmlEscapers.htmlEscaper().escape(String.valueOf(rawText));
    }

    private FilterDescriptionBuilder startTag(String tag) {
        tags.push(tag);
        content.append("<").append(tag).append(">");
        return this;
    }

    private FilterDescriptionBuilder tag(String tag) {
        content.append("<").append(tag).append(">");
        return this;
    }

    private FilterDescriptionBuilder endTag() {
        var tag = tags.pop();
        content.append("</").append(tag).append(">");
        return this;
    }

    private boolean isFinished() {
        return tags.isEmpty();
    }
}

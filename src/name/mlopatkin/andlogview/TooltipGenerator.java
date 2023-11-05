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
package name.mlopatkin.andlogview;

import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.search.text.TextHighlighter;

import com.google.common.collect.Range;
import com.google.common.html.HtmlEscapers;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TooltipGenerator implements TextHighlighter {
    private final String text;
    private final List<Range<Integer>> highlightRanges = new ArrayList<>();

    public TooltipGenerator(String text) {
        this.text = text;
    }

    @Override
    public void highlightText(int from, int to) {
        highlightRanges.add(Range.closedOpen(from, to));
    }

    private static final String highlightBackgroundColor = "yellow";
    private static final String highlightTextColor = "red";
    private static final String SPAN_BEGIN = String.format(
            "<span style='color: %s; background-color: %s'>", highlightTextColor, highlightBackgroundColor);
    private static final String SPAN_END = "</span>";
    private static final String BR = "<br>";
    private static final int WIDTH = Configuration.ui.tooltipMaxWidth();

    private static StringBuilder appendEnc(StringBuilder b, String text) {
        return b.append(HtmlEscapers.htmlEscaper().escape(text));
    }

    /**
     * Splits the text into several parts: non-highlighted - highlighted -
     * non-highlighted - ... Each part can be empty.
     *
     * @return parts of the text split accorded to highlighting
     */
    private String[] splitWithHighlights() {
        String[] result = new String[highlightRanges.size() * 2 + 1];
        int strPos = 0;
        int resultPos = 0;
        for (Range<Integer> r : highlightRanges) {
            result[resultPos++] = text.substring(strPos, r.lowerEndpoint());
            result[resultPos++] = text.substring(r.lowerEndpoint(), r.upperEndpoint());
            strPos = r.upperEndpoint();
        }
        result[resultPos] = text.substring(strPos);
        return result;
    }

    public String getTooltip() {
        StringBuilder result = new StringBuilder(text.length());
        result.append("<html>");
        String[] parts = splitWithHighlights();
        boolean shouldHighlight = false;
        int pos = 0;
        for (String s : parts) {
            if (shouldHighlight) {
                result.append(SPAN_BEGIN);
            }
            String remain = s;
            while (pos + remain.length() > WIDTH) {
                int indexOfBr = WIDTH - pos;
                String onThisLine = remain.substring(0, indexOfBr);
                remain = remain.substring(indexOfBr);
                appendEnc(result, onThisLine);
                result.append(BR);
                pos = 0;
            }
            appendEnc(result, remain);
            pos += remain.length();
            if (shouldHighlight) {
                result.append(SPAN_END);
            }
            shouldHighlight = !shouldHighlight;
        }
        if (result.toString().endsWith(BR)) {
            result.setLength(result.length() - BR.length());
        }
        if (result.toString().endsWith(BR + SPAN_END)) {
            result.replace(result.length() - (BR + SPAN_END).length(), result.length(), SPAN_END);
        }
        result.append("</html>");
        return result.toString();
    }
}

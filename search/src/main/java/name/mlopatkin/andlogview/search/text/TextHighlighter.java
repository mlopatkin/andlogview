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
package name.mlopatkin.andlogview.search.text;

/**
 * An implementation of this interface can mark portions of text as highlighted.
 */
public interface TextHighlighter {
    /**
     * Request highlighting of a text portion starting at char at {@code from} index (inclusive) and ending at char at
     * {@code end} index (exclusive). The {@code end} index can be outside the text bounds if the highlighting spans to
     * the end of text.
     *
     * @param from the index of the start of the highlighted portion of text
     * @param to the first index after the end of the highlighted portion of text
     */
    void highlightText(int from, int to);
}

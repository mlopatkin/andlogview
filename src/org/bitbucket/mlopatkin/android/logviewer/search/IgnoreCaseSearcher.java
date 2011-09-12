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
package org.bitbucket.mlopatkin.android.logviewer.search;

import org.apache.commons.lang3.StringUtils;
import org.bitbucket.mlopatkin.utils.MyStringUtils;

public class IgnoreCaseSearcher implements HighlightStrategy, SearchStrategy {
    private String textToSearch;
    private String begin;
    private String end;

    public IgnoreCaseSearcher(String text) {
        this.textToSearch = text;
    }

    @Override
    public boolean isStringMatched(String s) {
        return StringUtils.containsIgnoreCase(s, textToSearch);
    }

    @Override
    public void setHighlights(String begin, String end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    public String highlightOccurences(String text) {
        StringBuilder result = new StringBuilder(text);
        int pos = MyStringUtils.indexOfIgnoreCase(text, textToSearch);
        while (pos != MyStringUtils.NOT_FOUND && pos < result.length()) {
            String val = result.substring(pos, pos + textToSearch.length());
            result.replace(pos, pos + textToSearch.length(), begin + val + end);
            pos += val.length() + begin.length() + end.length();
            pos = MyStringUtils.indexOfIgnoreCase(result.toString(), textToSearch, pos);
        }
        return result.toString();
    }

}
/*
 * Copyright 2020 Mikhail Lopatkin
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
import name.mlopatkin.andlogview.test.AdaptingMatcher;

import org.hamcrest.Matcher;

import java.awt.Color;

public class FilterMatchers {
    public static Matcher<FilterFromDialog> hasTags(Matcher<Iterable<? extends String>> tagsMatcher) {
        return new AdaptingMatcher<>(FilterFromDialog::getTags, tagsMatcher);
    }

    public static Matcher<FilterFromDialog> hasPids(Matcher<Iterable<? extends Integer>> pidsMatcher) {
        return new AdaptingMatcher<>(FilterFromDialog::getPids, pidsMatcher);
    }

    public static Matcher<FilterFromDialog> hasApps(Matcher<Iterable<? extends String>> appsMatcher) {
        return new AdaptingMatcher<>(FilterFromDialog::getApps, appsMatcher);
    }

    public static Matcher<FilterFromDialog> hasMessage(Matcher<? super String> messageMatcher) {
        return new AdaptingMatcher<>(FilterFromDialog::getMessagePattern, messageMatcher);
    }

    public static Matcher<FilterFromDialog> hasPriority(Matcher<? super LogRecord.Priority> priorityMatcher) {
        return new AdaptingMatcher<>(FilterFromDialog::getPriority, priorityMatcher);
    }

    public static Matcher<FilterFromDialog> hasMode(Matcher<? super FilteringMode> modeMatcher) {
        return new AdaptingMatcher<>(FilterFromDialog::getMode, modeMatcher);
    }

    public static Matcher<FilterFromDialog> hasColor(Matcher<? super Color> colorMatcher) {
        return new AdaptingMatcher<>(FilterFromDialog::getHighlightColor, colorMatcher);
    }
}

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
import org.hamcrest.Matchers;

import java.awt.Color;

public class FilterMatchers {
    public static Matcher<FilterFromDialog> hasData(Matcher<? super FilterFromDialogData> dataMatcher) {
        return new AdaptingMatcher<>(FilterFromDialog::getData, dataMatcher);
    }

    public static Matcher<FilterFromDialog> hasMode(Matcher<? super FilteringMode> modeMatcher) {
        return new AdaptingMatcher<>(FilterFromDialog::getMode, modeMatcher);
    }

    public static Matcher<FilterFromDialogData> hasTags(Matcher<Iterable<? extends String>> tagsMatcher) {
        return new AdaptingMatcher<>(FilterFromDialogData::getTags, tagsMatcher);
    }

    public static Matcher<FilterFromDialogData> hasPids(Matcher<Iterable<? extends Integer>> pidsMatcher) {
        return new AdaptingMatcher<>(FilterFromDialogData::getPids, pidsMatcher);
    }

    public static Matcher<FilterFromDialogData> hasApps(Matcher<Iterable<? extends String>> appsMatcher) {
        return new AdaptingMatcher<>(FilterFromDialogData::getApps, appsMatcher);
    }

    public static Matcher<FilterFromDialogData> hasMessage(Matcher<? super String> messageMatcher) {
        return new AdaptingMatcher<>(FilterFromDialogData::getMessagePattern, messageMatcher);
    }

    public static Matcher<FilterFromDialogData> hasPriority(Matcher<? super LogRecord.Priority> priorityMatcher) {
        return new AdaptingMatcher<>(FilterFromDialogData::getPriority, priorityMatcher);
    }

    public static Matcher<FilterFromDialogData> hasColor(Matcher<? super Color> colorMatcher) {
        return new AdaptingMatcher<>(FilterFromDialogData::getHighlightColor, colorMatcher);
    }

    public static Matcher<FilterFromDialog> isEnabled() {
        return new AdaptingMatcher<>(FilterFromDialog::isEnabled, Matchers.is(true));
    }

    public static Matcher<FilterFromDialog> isDisabled() {
        return new AdaptingMatcher<>(FilterFromDialog::isEnabled, Matchers.is(false));
    }

    public static Matcher<FilterFromDialog> hasNoName() {
        return new AdaptingMatcher<>(FilterFromDialog::getName, Matchers.nullValue());
    }

    public static Matcher<FilterFromDialog> hasName(String name) {
        return new AdaptingMatcher<>(FilterFromDialog::getName, Matchers.is(name));
    }
}

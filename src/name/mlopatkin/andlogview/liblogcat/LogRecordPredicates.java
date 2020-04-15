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

package name.mlopatkin.andlogview.liblogcat;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.function.Predicate;

/**
 * Common predicates for LogRecords. All predicates throw NPE on null input.
 */
public final class LogRecordPredicates {
    private LogRecordPredicates() {}

    public static Predicate<LogRecord> matchTag(final Predicate<String> tagMatcher) {
        return input -> tagMatcher.test(checkNotNull(input).getTag());
    }

    public static Predicate<LogRecord> withPid(final int pid) {
        return input -> checkNotNull(input).getPid() == pid;
    }

    public static Predicate<LogRecord> moreSevereThan(final LogRecord.Priority priority) {
        return input -> checkNotNull(input).getPriority().compareTo(priority) >= 0;
    }

    public static Predicate<LogRecord> withAnyOfPids(List<Integer> pids) {
        final ImmutableSet<Integer> pidSet = ImmutableSet.copyOf(pids);

        return input -> pidSet.contains(checkNotNull(input).getPid());
    }

    public static Predicate<LogRecord> matchMessage(final Predicate<String> messageMatcher) {
        return input -> messageMatcher.test(checkNotNull(input).getMessage());
    }

    public static Predicate<LogRecord> matchAppName(final Predicate<String> appNameMatcher) {
        return input -> appNameMatcher.test(checkNotNull(input).getAppName());
    }

    public static Predicate<LogRecord> withBuffer(final LogRecord.Buffer buffer) {
        return input -> checkNotNull(input).getBuffer() == buffer;
    }
}

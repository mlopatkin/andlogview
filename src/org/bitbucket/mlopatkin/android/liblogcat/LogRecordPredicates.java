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

package org.bitbucket.mlopatkin.android.liblogcat;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

import org.bitbucket.mlopatkin.utils.FluentPredicate;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Common predicates for LogRecords. All predicates throw NPE on null input.
 */
@ParametersAreNonnullByDefault
public final class LogRecordPredicates {
    private LogRecordPredicates() {}

    public static FluentPredicate<LogRecord> matchTag(final Predicate<String> tagMatcher) {
        return new FluentPredicate<LogRecord>() {
            @Override
            public boolean apply(@Nullable LogRecord input) {
                return tagMatcher.apply(checkNotNull(input).getTag());
            }
        };
    }

    public static FluentPredicate<LogRecord> withPid(final int pid) {
        return new FluentPredicate<LogRecord>() {
            @Override
            public boolean apply(@Nullable LogRecord input) {
                return checkNotNull(input).getPid() == pid;
            }
        };
    }

    public static FluentPredicate<LogRecord> moreSevereThan(final LogRecord.Priority priority) {
        return new FluentPredicate<LogRecord>() {
            @Override
            public boolean apply(@Nullable LogRecord input) {
                return checkNotNull(input).getPriority().compareTo(priority) >= 0;
            }
        };
    }

    public static FluentPredicate<LogRecord> withAnyOfPids(List<Integer> pids) {
        final ImmutableSet<Integer> pidSet = ImmutableSet.copyOf(pids);

        return new FluentPredicate<LogRecord>() {
            @Override
            public boolean apply(@Nullable LogRecord input) {
                return pidSet.contains(checkNotNull(input).getPid());
            }
        };
    }

    public static FluentPredicate<LogRecord> matchMessage(final Predicate<String> messageMatcher) {
        return new FluentPredicate<LogRecord>() {
            @Override
            public boolean apply(@Nullable LogRecord input) {
                return messageMatcher.apply(checkNotNull(input).getMessage());
            }
        };
    }

    public static FluentPredicate<LogRecord> matchAppName(final Predicate<String> appNameMatcher) {
        return new FluentPredicate<LogRecord>() {
            @Override
            public boolean apply(@Nullable LogRecord input) {
                return appNameMatcher.apply(checkNotNull(input).getAppName());
            }
        };
    }

    public static FluentPredicate<LogRecord> withBuffer(final LogRecord.Buffer buffer) {
        return new FluentPredicate<LogRecord>() {
            @Override
            public boolean apply(@Nullable LogRecord input) {
                return checkNotNull(input).getBuffer() == buffer;
            }
        };
    }
}

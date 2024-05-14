/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.parsers.logcat;

import name.mlopatkin.andlogview.logmodel.Field;
import name.mlopatkin.andlogview.logmodel.LogRecord;

import com.google.common.collect.ImmutableSet;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;

/**
 * Metadata about the supported log formats.
 */
public enum Format {
    BRIEF("brief", DelegateBrief::new, Field.PRIORITY, Field.TAG, Field.PID, Field.MESSAGE),
    LONG("long", DelegateLong::new, Field.TIME, Field.PID, Field.TID, Field.PRIORITY, Field.TAG, Field.MESSAGE),
    PROCESS("process", DelegateProcess::new, Field.PRIORITY, Field.PID, Field.MESSAGE, Field.TAG),
    RAW("raw", Field.MESSAGE),
    STUDIO(null, DelegateStudio::new, Field.TIME, Field.PID, Field.TID, Field.APP_NAME, Field.PRIORITY, Field.TAG,
            Field.MESSAGE),
    TAG("tag", DelegateTag::new, Field.PRIORITY, Field.TAG, Field.MESSAGE),
    THREAD("thread", Field.PRIORITY, Field.PID, Field.TID, Field.MESSAGE),
    THREADTIME("threadtime", DelegateThreadTime::new, Field.TIME, Field.PID, Field.TID, Field.PRIORITY, Field.TAG,
            Field.MESSAGE),
    TIME("time", DelegateTime::new, Field.TIME, Field.PRIORITY, Field.TAG, Field.PID, Field.MESSAGE);

    private final @Nullable String formatName;
    @SuppressWarnings("ImmutableEnumChecker")
    private final @Nullable Function<LogcatParseEventsHandler, RegexLogcatParserDelegate> parserFactory;
    private final ImmutableSet<Field<?>> availableFields;

    Format(
            @Nullable String cmdFormatName,
            @Nullable Function<LogcatParseEventsHandler, RegexLogcatParserDelegate> parserFactory,
            Field<?>... availableFields) {
        this.formatName = cmdFormatName;
        this.parserFactory = parserFactory;
        this.availableFields = ImmutableSet.copyOf(availableFields);
    }

    Format(@Nullable String formatName, Field<?>... availableFields) {
        this(formatName, null, availableFields);
    }

    /**
     * Returns the name of the format to be used in {@code logcat -v ...} command line invocation. May throw if this
     * format is known to be unsupported by the logcat utility, like {@link #STUDIO}.
     * @return the name of the format to use in logcat command line
     * @throws UnsupportedOperationException if the format is not supported by logcat
     */
    public String getCmdFormatName() {
        if (formatName == null) {
            throw new UnsupportedOperationException("Format " + name() + " is not supported by logcat");
        }
        return formatName;
    }

    public final Set<Field<?>> getAvailableFields() {
        return availableFields;
    }

    public final boolean isSupported() {
        return parserFactory != null;
    }

    final RegexLogcatParserDelegate createParser(LogcatParseEventsHandler eventsHandler) {
        if (parserFactory != null) {
            return parserFactory.apply(eventsHandler);
        }
        throw new IllegalArgumentException("Unsupported format " + name());
    }

    /**
     * Creates a log record comparator, that only compares fields present in this format.
     *
     * @return the comparator
     */
    final Comparator<LogRecord> createComparator() {
        return getAvailableFields().stream()
                .map(Field::createComparator)
                .reduce(Comparator::thenComparing)
                .orElseThrow(() -> new AssertionError("Number of fields in " + this + " is empty, cannot reduce"));
    }
}

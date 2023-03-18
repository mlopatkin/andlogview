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

import com.google.common.collect.ImmutableSet;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Metadata about the supported log formats.
 */
public enum Format {
    BRIEF(DelegateBrief::new, Field.PRIORITY, Field.TAG, Field.PID, Field.MESSAGE),
    LONG(Field.TIME, Field.PID, Field.TID, Field.PRIORITY, Field.TAG, Field.MESSAGE),
    PROCESS(DelegateProcess::new, Field.PRIORITY, Field.PID, Field.MESSAGE, Field.TAG),
    RAW(Field.MESSAGE),
    STUDIO(DelegateStudio::new, Field.TIME, Field.PID, Field.TID, Field.APP_NAME, Field.PRIORITY, Field.TAG,
            Field.MESSAGE),
    TAG(DelegateTag::new, Field.PRIORITY, Field.TAG, Field.MESSAGE),
    THREAD(Field.PRIORITY, Field.PID, Field.TID, Field.MESSAGE),
    THREADTIME(DelegateThreadTime::new, Field.TIME, Field.PID, Field.TID, Field.PRIORITY, Field.TAG, Field.MESSAGE),
    TIME(DelegateTime::new, Field.TIME, Field.PRIORITY, Field.TAG, Field.PID, Field.MESSAGE);

    @SuppressWarnings("ImmutableEnumChecker")
    private final @Nullable Function<LogcatParseEventsHandler, RegexLogcatParserDelegate> parserFactory;
    private final ImmutableSet<Field> availableFields;

    Format(@Nullable Function<LogcatParseEventsHandler, RegexLogcatParserDelegate> parserFactory,
            Field... availableFields) {
        this.parserFactory = parserFactory;
        EnumSet<Field> fieldSet = EnumSet.noneOf(Field.class);
        fieldSet.addAll(Arrays.asList(availableFields));
        this.availableFields = ImmutableSet.copyOf(fieldSet);
    }

    Format(Field... availableFields) {
        this(null, availableFields);
    }

    public final Set<Field> getAvailableFields() {
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
}

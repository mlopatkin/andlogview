/*
 * Copyright 2015 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.logmodel;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.Immutable;

import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

/**
 * Fields in the log record.
 */
@Immutable
public final class Field<T extends @Nullable Comparable<T>> {
    // This is a poor man's enum. Enum cannot be generic and enum constants cannot have different types, so we have to
    // go back to the old ways here.

    /**
     * The timestamp. This is an optional field with {@code null} representing the missing value.
     */
    public static final Field<@Nullable Timestamp> TIME = new Field<>("TIME", LogRecord::getTime);

    /**
     * The PID. This is an optional field with {@link LogRecord#NO_ID} representing the missing value.
     */
    public static final Field<Integer> PID = new Field<>("PID", LogRecord::getPid);

    /**
     * The TID. This is an optional field with {@link LogRecord#NO_ID} representing the missing value.
     */
    public static final Field<Integer> TID = new Field<>("TID", LogRecord::getTid);

    /**
     * The priority.
     */
    public static final Field<LogRecord.Priority> PRIORITY = new Field<>("PRIORITY", LogRecord::getPriority);

    /**
     * The tag. This is an optional field with empty string representing the missing value.
     */
    public static final Field<String> TAG = new Field<>("TAG", LogRecord::getTag);

    /**
     * The message.
     */
    public static final Field<String> MESSAGE = new Field<>("MESSAGE", LogRecord::getMessage);

    /**
     * The buffer. This is an optional field with {@code null} representing the missing value.
     */
    public static final Field<LogRecord.@Nullable Buffer> BUFFER = new Field<>("BUFFER", LogRecord::getBuffer);

    /**
     * The app name. This is an optional field with empty string representing the missing value.
     */
    public static final Field<String> APP_NAME = new Field<>("APP_NAME", LogRecord::getAppName);

    private static class ValuesHolder {
        // A Holder class idiom to prevent initialization order issues with constants.
        private static ImmutableSet.@Nullable Builder<Field<?>> builder = ImmutableSet.builder();
    }

    private static final ImmutableSet<Field<?>> VALUES;

    static {
        // This block must come after all constants are declared.
        VALUES = Objects.requireNonNull(ValuesHolder.builder).build();
        // Free memory and prevent accidentally creating extra constants that won't make it into VALUES.
        ValuesHolder.builder = null;
    }

    private final String name;
    @SuppressWarnings("Immutable")
    private final Function<? super LogRecord, ? extends T> extractor;

    /**
     * Extracts the value of this field from the given record.
     *
     * @param record the record to get the value from
     * @return the value of the field of the record
     */
    public T getValue(LogRecord record) {
        return extractor.apply(record);
    }

    private Field(String name, Function<? super LogRecord, ? extends T> extractor) {
        this.name = name;
        this.extractor = extractor;
        Objects.requireNonNull(ValuesHolder.builder).add(this);
    }

    /**
     * Returns all available values of this "enum".
     *
     * @return the immutable set of values
     */
    public static ImmutableSet<Field<?>> values() {
        return VALUES;
    }

    /**
     * Creates a comparator that can be used to compare records by the value of the field. The returned comparator
     * doesn't accept {@code null} records, or records with {@code null} values of the field.
     *
     * @return the comparator
     */
    public Comparator<LogRecord> createComparator() {
        return new Comparator<>() {
            @Override
            public int compare(LogRecord o1, LogRecord o2) {
                var value1 = Objects.requireNonNull(getValue(o1));
                var value2 = Objects.requireNonNull(getValue(o2));
                return value1.compareTo(value2);
            }

            @Override
            public String toString() {
                return "Comparator<LogRecord>{" + name + "}";
            }
        };
    }
}

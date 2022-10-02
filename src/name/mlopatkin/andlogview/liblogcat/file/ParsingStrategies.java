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
package name.mlopatkin.andlogview.liblogcat.file;

import name.mlopatkin.andlogview.liblogcat.LogRecordParser;
import name.mlopatkin.andlogview.logmodel.Field;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

class ParsingStrategies {
    interface Strategy {
        @Nullable LogRecord parse(@Nullable Buffer buffer, String line, Map<Integer, String> pidToProcess);

        Set<Field> getAvailableFields();
    }

    static final ParsingStrategies.Strategy threadTime = new ParsingStrategies.Strategy() {
        @Override
        public @Nullable LogRecord parse(@Nullable Buffer buffer, String line, Map<Integer, String> pidToProcess) {
            return LogRecordParser.parseThreadTime(buffer, line, pidToProcess);
        }

        @Override
        public Set<Field> getAvailableFields() {
            return EnumSet.complementOf(EnumSet.of(Field.APP_NAME));
        }

        @Override
        public String toString() {
            return "ThreadTimeStrategy";
        }
    };
    static final ParsingStrategies.Strategy brief = new ParsingStrategies.Strategy() {
        @Override
        public @Nullable LogRecord parse(@Nullable Buffer buffer, String line, Map<Integer, String> pidToProcess) {
            return LogRecordParser.parseBrief(buffer, line, pidToProcess);
        }

        @Override
        public Set<Field> getAvailableFields() {
            return EnumSet.of(Field.PRIORITY, Field.TAG, Field.PID, Field.MESSAGE);
        }

        @Override
        public String toString() {
            return "BriefStrategy";
        }
    };

    static final ParsingStrategies.Strategy time = new ParsingStrategies.Strategy() {
        @Override
        public @Nullable LogRecord parse(@Nullable Buffer buffer, String line, Map<Integer, String> pidToProcess) {
            return LogRecordParser.parseTime(buffer, line, pidToProcess);
        }

        @Override
        public Set<Field> getAvailableFields() {
            return EnumSet.of(Field.TIME, Field.PRIORITY, Field.TAG, Field.PID, Field.MESSAGE);
        }

        @Override
        public String toString() {
            return "TimeStrategy";
        }
    };
    static final ParsingStrategies.Strategy androidStudio = new ParsingStrategies.Strategy() {
        @Override
        public @Nullable LogRecord parse(@Nullable Buffer buffer, String line, Map<Integer, String> pidToProcess) {
            return LogRecordParser.parseAndroidStudio(line);
        }

        @Override
        public Set<Field> getAvailableFields() {
            return EnumSet.of(
                    Field.TIME,
                    Field.PID,
                    Field.TID,
                    Field.APP_NAME,
                    Field.PRIORITY,
                    Field.TAG,
                    Field.MESSAGE);
        }

        @Override
        public String toString() {
            return "AndroidStudioStrategy";
        }
    };
    static final ParsingStrategies.Strategy[] supportedStrategies = {threadTime, brief, time, androidStudio};
}

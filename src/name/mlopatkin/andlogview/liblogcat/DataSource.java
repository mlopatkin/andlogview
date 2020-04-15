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
package name.mlopatkin.andlogview.liblogcat;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Interface that all log records producers like ADB or log files must
 * implement.
 */
public interface DataSource {
    /**
     * Sets the listener to which all output will be directed.
     *
     * @param listener the listener to set
     */
    void setLogRecordListener(RecordListener<LogRecord> listener);

    /**
     * Returns the utility class that performs conversion from PID to process
     * name.
     *
     * @return {@link Map} or {@code null} if this feature is
     *         not available
     */
    @Nullable Map<Integer, String> getPidToProcessConverter();

    /**
     * Disposes all resources of this {@link DataSource}. It becomes not usable.
     */
    void close();

    /**
     * Returns a set of the buffers available in this source.
     *
     * @return a set of buffers
     */
    EnumSet<LogRecord.Buffer> getAvailableBuffers();

    /**
     * @return the set of fields available in LogRecords that are produced by this data source
     */
    Set<Field> getAvailableFields();

    /**
     * Resets internal data structures and resends all available records into
     * the attached listener.
     *
     * @return {@code true} if the new are basically the same (e.g. reloading a
     *         file), {@code false} if the records are completely new (so
     *         bookmarks become invalid)
     */
    boolean reset();
}

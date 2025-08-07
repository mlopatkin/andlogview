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

package name.mlopatkin.andlogview.ui.logtable;

import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.logmodel.Field;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.TimeFormatUtils;
import name.mlopatkin.andlogview.logmodel.Timestamp;
import name.mlopatkin.andlogview.widgets.TableColumnBuilder;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Supported columns of the LogRecordTable.
 */
public enum Column {
    INDEX("row", "line") {
        @Override
        public Integer getValue(int rowIndex, LogRecord record) {
            return rowIndex + 1;
        }
    },
    TIME(Field.TIME, "time", "Time") {
        @Override
        public String getStrValue(int rowIndex, LogRecord record) {
            Timestamp time = record.getTime();
            assert time != null;
            return TimeFormatUtils.convertTimeToString(time);
        }
    },
    PID(Field.PID, "pid", "pid"),
    TID(Field.TID, "tid", "tid"),
    APP_NAME(Field.APP_NAME, "app", "Application"),
    PRIORITY(Field.PRIORITY, "priority", null) {
        @Override
        public String getStrValue(int rowIndex, LogRecord record) {
            return record.getPriority().getLetter();
        }
    },
    TAG(Field.TAG, "tag", "Tag"),
    // Message isn't toggleable so the user cannot disable everything.
    MESSAGE(Field.MESSAGE, "message", "Message", false);

    private final @Nullable Field<?> recordField;
    private final String columnName;
    private final @Nullable String title;
    private final boolean toggleable;

    /**
     * Constructor for computed column which doesn't correspond to a field in the logcat output. This column cannot be
     * toggled on or off.
     *
     * @param name short name that is used as a key in preferences
     * @param title user-visible title of the column (can be empty)
     */
    Column(String name, @Nullable String title) {
        this(null, name, title, false);
    }

    /**
     * Constructor for column that displays values from recordField of the logcat output. This column can be toggled on
     * or off with context menu.
     *
     * @param recordField the corresponding logcat field
     * @param name short name that is used as a key in preferences
     * @param title user-visible title of the column (can be empty)
     */
    Column(@Nullable Field<?> recordField, String name, @Nullable String title) {
        this(recordField, name, title, true);
    }

    /**
     * Generic constructor for column.
     *
     * @param recordField corresponding logcat field or null if there is no one
     * @param name short name that is used as a key in preferences
     * @param title user-visible title of the column (can be empty or null)
     * @param toggleable whether the visibility of the column can be toggled with popup menu
     */
    Column(@Nullable Field<?> recordField, String name, @Nullable String title, boolean toggleable) {
        this.recordField = recordField;
        this.columnName = name;
        this.title = title;
        this.toggleable = toggleable;
    }

    /**
     * Returns the record field that this column displays or {@code null} if this is a synthetic field.
     *
     * @return the record field
     */
    public @Nullable Field<?> getRecordField() {
        return recordField;
    }

    /**
     * Extracts a (raw) value from the record at the rowIndex. This method is intended to be used by {@linkplain
     * LogRecordTableModel}. This value is then passed to the renderer.
     *
     * @param rowIndex the row index in model
     * @param record the record
     * @return the value (can be of any type)
     */
    public @Nullable Object getValue(int rowIndex, LogRecord record) {
        return Objects.requireNonNull(recordField).getValue(record);
    }

    /**
     * Extracts a human-readable string representation from the record at the rowIndex.
     *
     * @param rowIndex the row index in model
     * @param record the record
     * @return the string representing a value
     */
    public String getStrValue(int rowIndex, LogRecord record) {
        return String.valueOf(getValue(rowIndex, record));
    }

    @SuppressWarnings("EnumOrdinal")
    private int getIndex() {
        return ordinal();
    }

    static Column getByColumnIndex(int index) {
        return values()[index];
    }

    /**
     * @return the list of all columns that are enabled by config file in the specified order
     */
    public static List<Column> getSelectedColumns() {
        List<String> columnKeys = Configuration.ui.columns();
        List<Column> columns = new ArrayList<>(columnKeys.size());

        for (String key : columnKeys) {
            // Can be replacing with map lookup if there is performance bottleneck
            for (Column c : values()) {
                if (c.columnName.equals(key)) {
                    columns.add(c);
                }
            }
        }
        return columns;
    }

    public static Set<Column> getColumnsForFields(Collection<Field<?>> fields) {
        Set<Column> columns = EnumSet.noneOf(Column.class);
        for (Column column : Column.values()) {
            if (fields.contains(column.recordField)) {
                columns.add(column);
            }
        }
        return columns;
    }

    TableColumnBuilder makeColumnBuilder() {
        TableColumnBuilder builder = new TableColumnBuilder(getIndex()).setIdentifier(this);
        if (title != null) {
            builder.setHeader(title);
        }

        return builder;
    }

    public boolean isToggleable() {
        return toggleable;
    }

    public String getColumnName() {
        return columnName;
    }

    public @Nullable String getTitle() {
        return title;
    }
}

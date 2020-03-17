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

package org.bitbucket.mlopatkin.android.logviewer.ui.logtable;

import org.bitbucket.mlopatkin.android.liblogcat.Field;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.TimeFormatUtils;
import org.bitbucket.mlopatkin.android.logviewer.config.Configuration;
import org.bitbucket.mlopatkin.android.logviewer.widgets.TableColumnBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

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
        public Date getValue(int rowIndex, LogRecord record) {
            return record.getTime();
        }

        @Override
        public String getStrValue(int rowIndex, LogRecord record) {
            return TimeFormatUtils.convertTimeToString(record.getTime());
        }
    },
    PID(Field.PID, "pid", "pid") {
        @Override
        public Integer getValue(int rowIndex, LogRecord record) {
            return record.getPid();
        }
    },
    TID(Field.TID, "tid", "tid") {
        @Override
        public Integer getValue(int rowIndex, LogRecord record) {
            return record.getTid();
        }
    },
    APP_NAME(Field.APP_NAME, "app", "Application") {
        @Override
        public String getValue(int rowIndex, LogRecord record) {
            return record.getAppName();

        }
    },
    PRIORITY(Field.PRIORITY, "priority", null) {
        @Override
        public LogRecord.Priority getValue(int rowIndex, LogRecord record) {
            return record.getPriority();
        }

        @Override
        public String getStrValue(int rowIndex, LogRecord record) {
            return record.getPriority().getLetter();
        }
    },
    TAG(Field.TAG, "tag", "Tag") {
        @Override
        public String getValue(int rowIndex, LogRecord record) {
            return record.getTag();
        }
    },
    // Message isn't toggleable so the user cannot disable everything.
    MESSAGE(Field.MESSAGE, "message", "Message", false) {
        @Override
        public String getValue(int rowIndex, LogRecord record) {
            return record.getMessage();
        }
    };

    @Nullable
    private final Field recordField;
    private final String columnName;
    @Nullable
    private final String title;
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
    Column(@Nullable Field recordField, String name, @Nullable String title) {
        this(recordField, name, title, true);
    }

    /**
     * Generic constructor for column.
     * @param recordField corresponding logcat field or null if there is no one
     * @param name short name that is used as a key in preferences
     * @param title user-visible title of the column (can be empty or null)
     * @param toggleable whether the visibility of the column can be toggled with popup menu
     */
    Column(@Nullable Field recordField, String name, @Nullable String title, boolean toggleable) {
        this.recordField = recordField;
        this.columnName = name;
        this.title = title;
        this.toggleable = toggleable;
    }

    /**
     * Extracts a (raw) value from the record at the rowIndex. This method is intended to be used by {@linkplain
     * LogRecordTableModel}. This value is then passed to the renderer.
     *
     * @param rowIndex the row index in model
     * @param record the record
     * @return the value (can be of any type)
     */
    public abstract Object getValue(int rowIndex, LogRecord record);

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

    // TODO: reduce visibility
    // For now it is used by ValueSearcher and in SearchResultsHighlightCell renderer
    public int getIndex() {
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

    public static Set<Column> getColumnsForFields(Collection<Field> fields) {
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

    @Nullable
    public String getTitle() {
        return title;
    }
}

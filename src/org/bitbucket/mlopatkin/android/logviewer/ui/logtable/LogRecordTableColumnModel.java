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
package org.bitbucket.mlopatkin.android.logviewer.ui.logtable;

import com.google.common.base.Preconditions;

import org.bitbucket.mlopatkin.android.logviewer.PidToProcessMapper;
import org.bitbucket.mlopatkin.android.logviewer.widgets.TableCellHelper;
import org.bitbucket.mlopatkin.android.logviewer.widgets.TableColumnBuilder;

import java.util.EnumMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * The column model for the main log table. It allows to temporarily hide columns.
 */
public class LogRecordTableColumnModel extends DefaultTableColumnModel {
    private final EnumMap<Column, TableColumn> columnsCache = new EnumMap<>(Column.class);

    private final TableCellEditor readOnlyCellEditor = TableCellHelper.createReadOnlyCellTextEditor();

    private final TableCellRenderer timeCellRenderer = new LogRecordTimeCellRenderer();
    private final TableCellRenderer priorityCellRenderer = new LogRecordPriorityCellRenderer();
    private final TableCellRenderer textCellRenderer = new HighlightCellRenderer();
    private final TableCellRenderer pidCellRender;

    private final CanonicalColumnOrder columnOrder;


    protected LogRecordTableColumnModel(PidToProcessMapper pidToProcessMapper, List<Column> availableColumns,
            CanonicalColumnOrder columnOrder) {
        pidCellRender = new ToolTippedPidCellRenderer(pidToProcessMapper);
        this.columnOrder = columnOrder;

        addTextColumn(Column.INDEX).setWidth(30).setMaxWidth(50);
        addTimeColumn(Column.TIME).setWidth(150).setMaxWidth(150);
        addPidColumn(Column.PID).setWidth(40).setMaxWidth(50);
        addTextColumn(Column.TID).setWidth(40).setMaxWidth(50);
        addPriorityColumn(Column.PRIORITY).setWidth(30).setMaxWidth(50);
        addTextColumn(Column.TAG).setWidth(120);
        addTextColumn(Column.APP_NAME).setWidth(150);
        addTextColumn(Column.MESSAGE).setWidth(1000);

        for (Column column : availableColumns) {
            showColumnFor(column);
        }
    }

    private TableColumnBuilder makeBuilder(Column column) {
        Preconditions.checkArgument(!columnsCache.containsKey(column), "Column %s already addded", column.name());
        TableColumnBuilder builder = column.makeColumnBuilder();
        columnsCache.put(column, builder.get());
        return builder.setEditor(readOnlyCellEditor);
    }

    private TableColumnBuilder addTimeColumn(Column column) {
        return makeBuilder(column).setRenderer(timeCellRenderer);
    }

    private TableColumnBuilder addPriorityColumn(Column column) {
        return makeBuilder(column).setRenderer(priorityCellRenderer);
    }

    private TableColumnBuilder addTextColumn(Column column) {
        return makeBuilder(column).setRenderer(textCellRenderer);
    }

    private TableColumnBuilder addPidColumn(Column column) {
        return makeBuilder(column).setRenderer(pidCellRender);
    }

    private void showColumnFor(Column column) {
        Preconditions.checkArgument(!isColumnVisible(column), "Column %s already addded", column.name());
        int desiredPosition = findPositionForColumn(column);
        addColumn(columnsCache.get(column));
        int actualPosition = getColumnCount() - 1;
        if (desiredPosition != actualPosition) {
            moveColumn(actualPosition, desiredPosition);
        }
    }

    private void hideColumnFor(Column column) {
        removeColumn(columnsCache.get(column));
    }

    private int findPositionForColumn(Column column) {
        for (int i = 0; i < getColumnCount(); ++i) {
            if (columnOrder.compare(getColumnForIndex(i), column) >= 0) {
                return i;
            }
        }
        return getColumnCount();
    }

    private Column getColumnForIndex(int columnIndex) {
        return (Column) getColumn(columnIndex).getIdentifier();
    }

    boolean isColumnVisible(Column column) {
        return tableColumns.stream().anyMatch(tc -> column.equals(tc.getIdentifier()));
    }

    void setColumnVisibility(Column column, boolean visible) {
        Preconditions.checkState(isColumnVisible(column) != visible,
                                 visible ? "Trying to show column that is here" : "Trying to hide hidden column");
        if (visible) {
            showColumnFor(column);
        } else {
            hideColumnFor(column);
        }
    }

    @Singleton
    public static class Factory {
        private final CanonicalColumnOrder columnOrder;

        @Inject
        public Factory(CanonicalColumnOrder columnOrder) {
            this.columnOrder = columnOrder;
        }

        public LogRecordTableColumnModel create(PidToProcessMapper pidToProcessMapper, List<Column> availableColumns) {
            return new LogRecordTableColumnModel(pidToProcessMapper, availableColumns, columnOrder);
        }
    }
}

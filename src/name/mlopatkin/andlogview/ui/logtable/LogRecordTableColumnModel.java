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
package name.mlopatkin.andlogview.ui.logtable;

import name.mlopatkin.andlogview.PidToProcessMapper;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;
import name.mlopatkin.andlogview.widgets.TableCellHelper;
import name.mlopatkin.andlogview.widgets.TableColumnBuilder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * The column model for the main log table. It allows to temporarily hide columns.
 */
public final class LogRecordTableColumnModel extends DefaultTableColumnModel implements ColumnTogglesModel {
    public interface ColumnOrderChangedListener {
        void onColumnOrderChanged(Column movedColumn, @Nullable Column nextColumn);
    }

    private final EnumMap<Column, TableColumn> columnsCache = new EnumMap<>(Column.class);

    private final TableCellEditor readOnlyCellEditor = TableCellHelper.createReadOnlyCellTextEditor();

    private final TableCellRenderer timeCellRenderer = new LogRecordTimeCellRenderer();
    private final TableCellRenderer priorityCellRenderer = new LogRecordPriorityCellRenderer();
    private final TableCellRenderer textCellRenderer = new HighlightCellRenderer();
    private final TableCellRenderer pidCellRender;

    private final ColumnOrder columnOrder;
    private final ColumnTogglesModel columnTogglesModel;
    private final Subject<ColumnOrderChangedListener> orderChangedListeners = new Subject<>();

    public LogRecordTableColumnModel(@Nullable PidToProcessMapper pidToProcessMapper,
            Collection<Column> availableColumns, ColumnOrder columnOrder, Set<Column> visibleColumns) {
        this(pidToProcessMapper, columnOrder, new ColumnTogglesModel() {
            private final HashSet<Column> visible = new HashSet<>(visibleColumns);

            @Override
            public boolean isColumnAvailable(Column column) {
                return availableColumns.contains(column);
            }

            @Override
            public boolean isColumnVisible(Column column) {
                return isColumnAvailable(column) && visible.contains(column);
            }

            @Override
            public void setColumnVisibility(Column column, boolean isVisible) {
                if (isVisible) {
                    visible.add(column);
                } else {
                    visible.remove(column);
                }
            }
        });
    }

    public LogRecordTableColumnModel(@Nullable PidToProcessMapper pidToProcessMapper, ColumnOrder columnOrder,
            ColumnTogglesModel columnTogglesModel) {
        this.pidCellRender = new ToolTippedPidCellRenderer(pidToProcessMapper);
        this.columnOrder = columnOrder;
        this.columnTogglesModel = columnTogglesModel;

        addTextColumn(Column.INDEX).setWidth(30).setMaxWidth(50);
        addTimeColumn(Column.TIME).setWidth(180).setMaxWidth(250);
        addPidColumn(Column.PID).setWidth(40).setMaxWidth(100);
        addTextColumn(Column.TID).setWidth(40).setMaxWidth(100);
        addPriorityColumn(Column.PRIORITY).setWidth(30).setMaxWidth(80);
        addTextColumn(Column.TAG).setWidth(120);
        addTextColumn(Column.APP_NAME).setWidth(150);
        addTextColumn(Column.MESSAGE).setWidth(1000);

        for (Column column : columnOrder) {
            if (columnTogglesModel.isColumnVisible(column)) {
                showColumnFor(column);
            }
        }
    }

    @VisibleForTesting
    static LogRecordTableColumnModel createForTest(Collection<Column> availableColumns) {
        return new LogRecordTableColumnModel(
                null, availableColumns, ColumnOrder.canonical(), EnumSet.allOf(Column.class));
    }

    private TableColumnBuilder makeBuilder(Column column) {
        Preconditions.checkArgument(!columnsCache.containsKey(column), "Column %s already added", column.name());
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
        Preconditions.checkArgument(!isColumnShown(column), "Column %s already added", column.name());
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

    @Override
    public boolean isColumnAvailable(Column column) {
        return columnTogglesModel.isColumnAvailable(column);
    }

    @Override
    public boolean isColumnVisible(Column column) {
        return columnTogglesModel.isColumnVisible(column);
    }

    private boolean isColumnShown(Column column) {
        return tableColumns.stream().anyMatch(tc -> column.equals(tc.getIdentifier()));
    }

    @Override
    public void setColumnVisibility(Column column, boolean visible) {
        Preconditions.checkState(isColumnVisible(column) != visible,
                visible ? "Trying to show column that is here" : "Trying to hide hidden column");
        if (visible) {
            showColumnFor(column);
        } else {
            hideColumnFor(column);
        }
        columnTogglesModel.setColumnVisibility(column, visible);
    }

    @Override
    public void moveColumn(int columnIndex, int newIndex) {
        Column movingColumn = getColumnForIndex(columnIndex);
        super.moveColumn(columnIndex, newIndex);
        if (columnIndex != newIndex) {
            int nextColumnIndex = newIndex + 1;
            Column nextColumn = nextColumnIndex < getColumnCount() ? getColumnForIndex(nextColumnIndex) : null;
            for (ColumnOrderChangedListener orderChangedListener : orderChangedListeners) {
                orderChangedListener.onColumnOrderChanged(movingColumn, nextColumn);
            }
        }
    }

    public Observable<ColumnOrderChangedListener> asColumnOrderChangeObservable() {
        return orderChangedListeners.asObservable();
    }
}

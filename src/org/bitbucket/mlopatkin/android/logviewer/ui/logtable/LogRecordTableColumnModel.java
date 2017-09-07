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
import java.util.EnumSet;
import java.util.List;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * The column model for the main log table. It allows to temporarily hide columns.
 */
public class LogRecordTableColumnModel extends DefaultTableColumnModel {
    private final EnumMap<Column, TableColumn> columnsCache = new EnumMap<>(Column.class);
    private final EnumSet<Column> activeColumns = EnumSet.noneOf(Column.class);

    private final TableCellEditor readOnlyCellEditor = TableCellHelper.createReadOnlyCellTextEditor();

    private final TableCellRenderer timeCellRenderer = new LogRecordTimeCellRenderer();
    private final TableCellRenderer priorityCellRenderer = new LogRecordPriorityCellRenderer();
    private final TableCellRenderer textCellRenderer = new HighlightCellRenderer();
    private final TableCellRenderer pidCellRender;


    protected LogRecordTableColumnModel(PidToProcessMapper pidToProcessMapper, List<Column> columns) {
        pidCellRender = new ToolTippedPidCellRenderer(pidToProcessMapper);

        addTextColumn(Column.INDEX).setWidth(30).setMaxWidth(50);
        addTimeColumn(Column.TIME).setWidth(150).setMaxWidth(150);
        addPidColumn(Column.PID).setWidth(40).setMaxWidth(50);
        addTextColumn(Column.TID).setWidth(40).setMaxWidth(50);
        addPriorityColumn(Column.PRIORITY).setWidth(30).setMaxWidth(50);
        addTextColumn(Column.TAG).setWidth(120);
        addTextColumn(Column.APP_NAME).setWidth(150);
        addTextColumn(Column.MESSAGE).setWidth(1000);

        for (Column column : columns) {
            addColumnFor(column);
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

    private void addColumnFor(Column column) {
        Preconditions.checkArgument(!activeColumns.contains(column), "Column %s already addded", column.name());
        activeColumns.add(column);
        addColumn(columnsCache.get(column));
    }

    private void hideColumnFor(Column column) {
        removeColumn(columnsCache.get(column));
        activeColumns.remove(column);
    }

    public static LogRecordTableColumnModel create(PidToProcessMapper pidToProcessMapper, List<Column> columns) {
        return new LogRecordTableColumnModel(pidToProcessMapper, columns);
    }

    boolean isColumnVisible(Column column) {
        return activeColumns.contains(column);
    }

    void setColumnVisibility(Column column, boolean isSelected) {
        Preconditions.checkState(activeColumns.contains(column) != isSelected,
                                 isSelected ? "Trying to show column that is here" : "Trying to hide hidden column");
        if (isSelected) {
            addColumnFor(column);
        } else {
            hideColumnFor(column);
        }
    }
}

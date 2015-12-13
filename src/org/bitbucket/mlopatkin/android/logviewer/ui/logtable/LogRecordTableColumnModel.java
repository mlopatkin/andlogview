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
import java.util.Map;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class LogRecordTableColumnModel extends DefaultTableColumnModel {

    protected static class Builder {
        private final TableCellEditor readOnlyCellEditor = TableCellHelper.createReadOnlyCellTextEditor();

        private final TableCellRenderer timeCellRenderer = new LogRecordTimeCellRenderer();
        private final TableCellRenderer priorityCellRenderer = new LogRecordPriorityCellRenderer();
        private final TableCellRenderer textCellRenderer = new HighlightCellRenderer();
        private final TableCellRenderer pidCellRender;

        private final Map<Column, TableColumnBuilder> columnBuilders = new EnumMap<>(Column.class);

        protected Builder(PidToProcessMapper mapper) {
            pidCellRender = new ToolTippedPidCellRenderer(mapper);
        }

        private TableColumnBuilder addColumn(Column column) {
            Preconditions.checkArgument(!columnBuilders.containsKey(column), "Column %s already addded", column.name());
            TableColumnBuilder columnBuilder = column.makeColumnBuilder();
            columnBuilders.put(column, columnBuilder);
            columnBuilder.setEditor(readOnlyCellEditor);
            return columnBuilder;
        }

        public TableColumnBuilder addTimeColumn(Column column) {
            return addColumn(column).setRenderer(timeCellRenderer);
        }

        public TableColumnBuilder addPriorityColumn(Column column) {
            return addColumn(column).setRenderer(priorityCellRenderer);
        }

        public TableColumnBuilder addTextColumn(Column column) {
            return addColumn(column).setRenderer(textCellRenderer);
        }

        public TableColumnBuilder addPidColumn(Column column) {
            return addColumn(column).setRenderer(pidCellRender);
        }

        TableColumnBuilder getBuilder(Column column) {
            return columnBuilders.get(column);
        }
    }

    protected LogRecordTableColumnModel(Builder builder, List<Column> columns) {
        for (Column column : columns) {
            addColumn(builder.getBuilder(column).build());
        }
    }


    protected static Builder makeDefaultBuilder(PidToProcessMapper mapper) {
        Builder b = new Builder(mapper);
        b.addTimeColumn(Column.TIME).setWidth(150).setMaxWidth(150);
        b.addPidColumn(Column.PID).setWidth(40).setMaxWidth(50);
        b.addTextColumn(Column.TID).setWidth(40).setMaxWidth(50);
        b.addPriorityColumn(Column.PRIORITY).setWidth(30).setMaxWidth(50);
        b.addTextColumn(Column.TAG).setWidth(120);
        b.addTextColumn(Column.APP_NAME).setWidth(150);
        b.addTextColumn(Column.MESSAGE).setWidth(1000);
        return b;
    }

    public static LogRecordTableColumnModel create(PidToProcessMapper pidToProcessMapper, List<Column> columns) {
        return new LogRecordTableColumnModel(makeDefaultBuilder(pidToProcessMapper), columns);
    }
}

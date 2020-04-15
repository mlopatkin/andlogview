/*
 * Copyright 2017 Mikhail Lopatkin
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

import com.google.common.collect.ImmutableList;

import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableColumnModel.ColumnOrderChangedListener;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import static org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TableColumnTestUtils.areTableColumnsFor;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class LogRecordTableColumnModelTest {
    @Test
    public void testModelCanDisplayAllColumns() throws Exception {
        List<Column> columns = Arrays.asList(Column.values());
        LogRecordTableColumnModel model = LogRecordTableColumnModel.createForTest(columns);

        assertThat("All columns should be here", getColumns(model), areTableColumnsFor(columns));
    }

    @Test
    public void testModelOnlyContainsColumnsPasssedAsInputInSortedOrder() throws Exception {
        ImmutableList<Column> columns = ImmutableList.of(Column.PID, Column.MESSAGE, Column.APP_NAME);
        LogRecordTableColumnModel model = LogRecordTableColumnModel.createForTest(columns);

        assertThat(getColumns(model), areTableColumnsFor(columns.stream().sorted().collect(Collectors.toList())));
    }

    @Test
    public void testIsColumnVisible() throws Exception {
        ImmutableList<Column> columns = ImmutableList.of(Column.PID);
        LogRecordTableColumnModel model = LogRecordTableColumnModel.createForTest(columns);

        assertTrue(model.isColumnVisible(Column.PID));
        assertFalse(model.isColumnVisible(Column.TIME));
    }

    @Test
    public void testHideVisibleColumn() throws Exception {
        ImmutableList<Column> columns = ImmutableList.of(Column.PID, Column.APP_NAME);
        LogRecordTableColumnModel model = LogRecordTableColumnModel.createForTest(columns);

        model.setColumnVisibility(Column.PID, false);

        assertThat(getColumns(model), areTableColumnsFor(Column.APP_NAME));
    }

    @Test
    public void testToggleFirstColumnKeepsItPosition() throws Exception {
        ImmutableList<Column> columns = ImmutableList.of(Column.PID, Column.APP_NAME, Column.MESSAGE);
        LogRecordTableColumnModel model = LogRecordTableColumnModel.createForTest(columns);

        model.setColumnVisibility(Column.PID, false);
        model.setColumnVisibility(Column.PID, true);

        assertThat(getColumns(model), areTableColumnsFor(columns));
    }

    @Test
    public void testToggleLastColumnKeepsItPosition() throws Exception {
        ImmutableList<Column> columns = ImmutableList.of(Column.PID, Column.APP_NAME, Column.MESSAGE);
        LogRecordTableColumnModel model = LogRecordTableColumnModel.createForTest(columns);

        model.setColumnVisibility(Column.MESSAGE, false);
        model.setColumnVisibility(Column.MESSAGE, true);

        assertThat(getColumns(model), areTableColumnsFor(columns));
    }

    @Test
    public void testToggleMiddleColumnKeepsItPosition() throws Exception {
        ImmutableList<Column> columns = ImmutableList.of(Column.PID, Column.APP_NAME, Column.MESSAGE);
        LogRecordTableColumnModel model = LogRecordTableColumnModel.createForTest(columns);

        model.setColumnVisibility(Column.APP_NAME, false);
        model.setColumnVisibility(Column.APP_NAME, true);

        assertThat(getColumns(model), areTableColumnsFor(columns));
    }

    private static Iterable<TableColumn> getColumns(TableColumnModel model) {
        return Collections.list(model.getColumns());
    }

    @Test
    public void testColumnOrderChangeListenerIsInvokedWhenFirstColumnMovedToMiddle() {
        ImmutableList<Column> columns = ImmutableList.of(Column.PID, Column.APP_NAME, Column.MESSAGE);
        LogRecordTableColumnModel model = LogRecordTableColumnModel.createForTest(columns);
        ColumnOrderChangedListener listener = mock(ColumnOrderChangedListener.class);

        model.asColumnOrderChangeObservable().addObserver(listener);

        model.moveColumn(0, 1);

        verify(listener).onColumnOrderChanged(Column.PID, Column.MESSAGE);
    }

    @Test
    public void testObserverNotCalledWhenMoveIsNoop() {
        ImmutableList<Column> columns = ImmutableList.of(Column.PID, Column.APP_NAME, Column.MESSAGE);
        LogRecordTableColumnModel model = LogRecordTableColumnModel.createForTest(columns);
        ColumnOrderChangedListener listener = mock(ColumnOrderChangedListener.class);

        model.asColumnOrderChangeObservable().addObserver(listener);

        model.moveColumn(0, 0);
        verify(listener, never()).onColumnOrderChanged(any(), any());
    }

    @Test
    public void testColumnOrderChangeListenerIsInvokedWhenLastColumnIsMovedToFirst() {
        ImmutableList<Column> columns = ImmutableList.of(Column.PID, Column.APP_NAME, Column.MESSAGE);
        LogRecordTableColumnModel model = LogRecordTableColumnModel.createForTest(columns);
        ColumnOrderChangedListener listener = mock(ColumnOrderChangedListener.class);

        model.asColumnOrderChangeObservable().addObserver(listener);

        model.moveColumn(2, 0);

        verify(listener).onColumnOrderChanged(Column.MESSAGE, Column.PID);
    }

    @Test
    public void testColumnOrderChangeListenerIsInvokedWhenMidColumnIsMovedToLast() {
        ImmutableList<Column> columns = ImmutableList.of(Column.PID, Column.APP_NAME, Column.MESSAGE);
        LogRecordTableColumnModel model = LogRecordTableColumnModel.createForTest(columns);
        ColumnOrderChangedListener listener = mock(ColumnOrderChangedListener.class);

        model.asColumnOrderChangeObservable().addObserver(listener);

        model.moveColumn(1, 2);

        verify(listener).onColumnOrderChanged(Column.APP_NAME, null);
    }
}

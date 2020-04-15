/*
 * Copyright 2020 Mikhail Lopatkin
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

import org.bitbucket.mlopatkin.android.logviewer.test.TestData;
import org.junit.Before;
import org.junit.Test;

import java.awt.EventQueue;
import java.util.Arrays;

import javax.swing.JTable;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SelectedRowsImplTest {

    private LogRecordTableModel model;
    private JTable table;

    @Before
    @SuppressWarnings("NullAway")
    public void setUp() throws Exception {
        // TODO(mlopatkin) Add custom runner to allow writing tests without invokeAndWait
        EventQueue.invokeAndWait(() -> {
            model = new LogRecordTableModel();
            model.addRecords(Arrays.asList(TestData.RECORD1, TestData.RECORD2));
            table = new JTable(model);
        });

    }

    @Test
    public void testReturnFirstElementSelection() throws Exception {
        EventQueue.invokeAndWait(() -> {
            SelectedRowsImpl selectedRows = new SelectedRowsImpl(table, model);
            table.setRowSelectionInterval(0, 0);

            assertThat(selectedRows.getSelectedRows(), contains(model.getRow(0)));
        });
    }

    @Test
    public void testReturnSecondElementSelection() throws Exception {
        EventQueue.invokeAndWait(() -> {
            SelectedRowsImpl selectedRows = new SelectedRowsImpl(table, model);
            table.setRowSelectionInterval(1, 1);

            assertThat(selectedRows.getSelectedRows(), contains(model.getRow(1)));
        });
    }

    @Test
    public void testReturnAllElementSelection() throws Exception {
        EventQueue.invokeAndWait(() -> {
            SelectedRowsImpl selectedRows = new SelectedRowsImpl(table, model);
            table.setRowSelectionInterval(0, 1);

            assertThat(selectedRows.getSelectedRows(), contains(model.getRow(0), model.getRow(1)));
        });
    }

    @Test
    public void testReturnNoneElementSelection() throws Exception {
        EventQueue.invokeAndWait(() -> {
            SelectedRowsImpl selectedRows = new SelectedRowsImpl(table, model);
            table.clearSelection();

            assertThat(selectedRows.getSelectedRows(), is(empty()));
        });
    }
}

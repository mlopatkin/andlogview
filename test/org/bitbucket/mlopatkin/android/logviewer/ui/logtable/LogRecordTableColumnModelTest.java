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

import org.bitbucket.mlopatkin.android.logviewer.PidToProcessMapper;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import static org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TableColumnTestUtils.areTableColumnsFor;
import static org.junit.Assert.assertThat;

public class LogRecordTableColumnModelTest {

    private PidToProcessMapper mapper = pid -> "";

    @Test
    public void testModelCanDisplayAllColumns() throws Exception {
        List<Column> columns = Arrays.asList(Column.values());
        LogRecordTableColumnModel model =
                new LogRecordTableColumnModel(mapper, columns);

        assertThat("All columns should be here", getColumns(model), areTableColumnsFor(columns));
    }

    @Test
    public void testTableColumnModelOnlyContainsColumnsPasssedAsInput() throws Exception {
        ImmutableList<Column> columns = ImmutableList.of(Column.PID, Column.APP_NAME);
        LogRecordTableColumnModel model =
                new LogRecordTableColumnModel(mapper, columns);

        assertThat(getColumns(model), areTableColumnsFor(columns));
    }

    private static Iterable<TableColumn> getColumns(TableColumnModel model) {
        return Collections.list(model.getColumns());
    }
}

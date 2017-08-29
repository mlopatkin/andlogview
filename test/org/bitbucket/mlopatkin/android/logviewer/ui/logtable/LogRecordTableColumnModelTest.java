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
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class LogRecordTableColumnModelTest {

    private PidToProcessMapper mapper = pid -> "";

    @Test
    public void testModelCanDisplayAllColumns() throws Exception {
        new LogRecordTableColumnModel(mapper, Arrays.asList(Column.values()));
    }

    @Test
    public void testTableColumnModelOnlyContainsColumnsPasssedAsInput() throws Exception {
        LogRecordTableColumnModel model =
                new LogRecordTableColumnModel(mapper, ImmutableList.of(Column.PID, Column.APP_NAME));

        Assert.assertEquals(2, model.getColumnCount());
    }
}

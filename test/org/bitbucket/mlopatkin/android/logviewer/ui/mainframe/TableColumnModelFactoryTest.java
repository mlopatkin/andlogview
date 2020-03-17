/*
 * Copyright 2018 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.mainframe;

import com.google.common.collect.ImmutableList;

import org.bitbucket.mlopatkin.android.logviewer.PidToProcessMapper;
import org.bitbucket.mlopatkin.android.logviewer.config.ConfigStorage;
import org.bitbucket.mlopatkin.android.logviewer.config.FakeDefaultConfigStorage;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.ColumnTogglesModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TogglesModelTestUtils.availableColumns;
import static org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TogglesModelTestUtils.visibleColumns;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class TableColumnModelFactoryTest {

    private ConfigStorage mockStorage = new FakeDefaultConfigStorage();
    @Mock
    private PidToProcessMapper mockMapper;

    private TableColumnModelFactory factory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        factory = new TableColumnModelFactory(new ColumnPrefs.Factory(mockStorage).loadFromConfig());
    }

    @Test
    public void createdModelRespectsAvaiableColumns() {
        ColumnTogglesModel columnModel =
                factory.create(mockMapper, ImmutableList.of(Column.APP_NAME, Column.INDEX, Column.MESSAGE));

        assertThat(columnModel, availableColumns(containsInAnyOrder(Column.APP_NAME, Column.MESSAGE)));
        assertThat(columnModel, visibleColumns(containsInAnyOrder(Column.APP_NAME, Column.MESSAGE)));
    }
}

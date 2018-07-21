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

package org.bitbucket.mlopatkin.android.logviewer;

import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DataSourceHolderTest {
    @Test
    public void testSetDataSource_invokesCallbackWhenCalledForTheFirstTime() throws Exception {
        DataSource newDataSource = mock(DataSource.class);
        DataSourceHolder.Observer observer = mock(DataSourceHolder.Observer.class);

        DataSourceHolder holder = new DataSourceHolder();
        holder.asObservable().addObserver(observer);

        holder.setDataSource(newDataSource);

        verify(observer).onDataSourceChanged(isNull(), eq(newDataSource));
    }

    @Test
    public void testCallbackIsInvokedAfterDataSourceChange() throws Exception {
        DataSource oldDataSource = mock(DataSource.class);
        DataSource newDataSource = mock(DataSource.class);

        AtomicReference<DataSource> dataSourceInCallback = new AtomicReference<>();

        DataSourceHolder holder = new DataSourceHolder();
        holder.setDataSource(oldDataSource);

        holder.asObservable().addObserver((a1, a2) -> dataSourceInCallback.set(holder.getDataSource()));
        holder.setDataSource(newDataSource);

        assertEquals(newDataSource, dataSourceInCallback.get());
    }
}

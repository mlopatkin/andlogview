/*
 * Copyright 2021 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.ui.status;

import name.mlopatkin.andlogview.DataSourceHolder;
import name.mlopatkin.andlogview.liblogcat.DataSource;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A presenter for the status bar entry that shows the status of the current data source.
 */
public class SourceStatusPresenter {
    private final DataSourceHolder dataSourceHolder;
    private final View view;

    interface View {
        void showWaitingStatus();
        void showSourceStatus(String status);
    }

    public SourceStatusPresenter(DataSourceHolder dataSourceHolder, View view) {
        this.dataSourceHolder = dataSourceHolder;
        this.view = view;
    }

    public void init() {
        dataSourceHolder.asObservable().addObserver(this::onDataSourceChanged);
        updateSourceStatus();
    }

    // TODO(mlopatkin): having this public is kind of lame
    public void updateSourceStatus() {
        DataSource dataSource = dataSourceHolder.getDataSource();
        if (dataSource != null) {
            view.showSourceStatus(dataSource.toString());
        } else {
            view.showWaitingStatus();
        }
    }

    private void onDataSourceChanged(@Nullable DataSource oldSource, DataSource newSource) {
        updateSourceStatus();
    }
}

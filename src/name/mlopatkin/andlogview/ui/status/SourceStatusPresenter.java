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
import name.mlopatkin.andlogview.logmodel.DataSource;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.utils.events.Observable;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

import javax.inject.Inject;

/**
 * A presenter for the status bar entry that shows the status of the current data source.
 */
@MainFrameScoped
public class SourceStatusPresenter {
    private final DataSourceHolder dataSourceHolder;
    private final View view;

    interface View {
        void showWaitingStatus();
        void showSourceStatus(String status);
        Observable<Consumer<SourceStatusPopupMenuView>> popupMenuAction();
    }

    @Inject
    public SourceStatusPresenter(DataSourceHolder dataSourceHolder, View view,
            SourcePopupMenuPresenter popupMenuPresenter) {
        this.dataSourceHolder = dataSourceHolder;
        this.view = view;
        view.popupMenuAction().addObserver(popupMenuPresenter::showPopupMenuIfNeeded);
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

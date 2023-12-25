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
import name.mlopatkin.andlogview.liblogcat.ddmlib.AdbDataSource;
import name.mlopatkin.andlogview.logmodel.DataSource;
import name.mlopatkin.andlogview.ui.device.AdbServicesStatus;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.utils.CommonChars;
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
    private final AdbServicesStatus adbServicesStatus;
    private final View view;
    private final AdbDataSource.StateObserver adbDataSourceObserver = new AdbDataSource.StateObserver() {
        @Override
        public void onDataSourceClosed() {
            updateSourceStatus();
        }
    };

    interface View {
        void showWaitingStatus(String statusText);

        void showSourceStatus(String status);

        Observable<Consumer<SourceStatusPopupMenuView>> popupMenuAction();
    }

    @Inject
    SourceStatusPresenter(
            DataSourceHolder dataSourceHolder,
            AdbServicesStatus adbServicesStatus,
            View view,
            SourcePopupMenuPresenter popupMenuPresenter) {
        this.dataSourceHolder = dataSourceHolder;
        this.adbServicesStatus = adbServicesStatus;
        this.view = view;

        view.popupMenuAction().addObserver(popupMenuPresenter::showPopupMenuIfNeeded);
    }

    @Inject
    void init() {
        adbServicesStatus.asObservable().addObserver(this::onAdbServicesStatusChanged);
        dataSourceHolder.asObservable().addObserver(this::onDataSourceChanged);
        if (dataSourceHolder.getDataSource() instanceof AdbDataSource adbDataSource) {
            adbDataSource.asStateObservable().addObserver(adbDataSourceObserver);
        }
        updateSourceStatus();
    }

    private void onAdbServicesStatusChanged(AdbServicesStatus.StatusValue statusValue) {
        updateSourceStatus();
    }

    private void updateSourceStatus() {
        var dataSource = dataSourceHolder.getDataSource();
        if (dataSource != null) {
            view.showSourceStatus(dataSource.toString());
        } else {
            view.showWaitingStatus(evaluateSourceStatus());
        }
    }

    private String evaluateSourceStatus() {
        assert dataSourceHolder.getDataSource() == null;

        var adbStatus = adbServicesStatus.getStatus();
        if (adbStatus instanceof AdbServicesStatus.NotInitialized
                || adbStatus instanceof AdbServicesStatus.Initializing) {
            return "Initializing ADB" + CommonChars.ELLIPSIS;
        }
        if (adbStatus instanceof AdbServicesStatus.InitFailed failure) {
            return "Failed to initialize ADB: " + failure.getFailureMessage();
        }
        return "Waiting for a device" + CommonChars.ELLIPSIS;
    }

    private void onDataSourceChanged(@Nullable DataSource oldSource, DataSource newSource) {
        if (oldSource instanceof AdbDataSource adbDataSource) {
            adbDataSource.asStateObservable().removeObserver(adbDataSourceObserver);
        }
        if (newSource instanceof AdbDataSource adbDataSource) {
            adbDataSource.asStateObservable().addObserver(adbDataSourceObserver);
        }
        updateSourceStatus();
    }
}

/*
 * Copyright 2023 the Andlogview authors
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

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.DataSourceHolder;
import name.mlopatkin.andlogview.logmodel.DataSource;
import name.mlopatkin.andlogview.ui.device.AdbServicesStatus;
import name.mlopatkin.andlogview.utils.MockUiThreadScheduler;
import name.mlopatkin.andlogview.utils.events.Subject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

@ExtendWith(MockitoExtension.class)
class SourceStatusPresenterTest {

    @Mock
    DataSourceHolder dataSourceHolder;

    @Mock
    AdbServicesStatus adbServicesStatus;

    @Mock
    SourceStatusPresenter.View view;

    @Mock
    SourcePopupMenuPresenter popupMenuPresenter;

    final MockUiThreadScheduler uiScheduler = new MockUiThreadScheduler();

    final Subject<AdbServicesStatus.Observer> adbServicesStatusObservers = new Subject<>();
    final Subject<DataSourceHolder.Observer> dataSourceHolderObservers = new Subject<>();
    final Subject<Consumer<SourceStatusPopupMenuView>> viewObservers = new Subject<>();

    @BeforeEach
    void setUp() {
        lenient().when(adbServicesStatus.asObservable()).thenReturn(adbServicesStatusObservers.asObservable());
        lenient().when(dataSourceHolder.asObservable()).thenReturn(dataSourceHolderObservers.asObservable());
        lenient().when(view.popupMenuAction()).thenReturn(viewObservers.asObservable());

        lenient().when(adbServicesStatus.getStatus()).thenReturn(AdbServicesStatus.StatusValue.notInitialized());
    }

    @Test
    void showsSourceStatusWhenCreatedWithDataSource() {
        withDataSource("test");

        createPresenter();

        verify(view).showSourceStatus("test");
    }

    @Test
    void showsWaitingStatusWhenNoDataSource() {
        withAdbInitialized();

        createPresenter();

        verify(view).showWaitingStatus(contains("Waiting"));
    }

    @Test
    void showsAdbInitializingStatus() {
        withAdbInitializing();

        createPresenter();

        verify(view).showWaitingStatus(contains("Initializing ADB"));
    }

    @Test
    void showsAdbFailureStatus() {
        withAdbFailedToInitialize("some failure");

        createPresenter();

        verify(view).showWaitingStatus(contains("Failed to initialize ADB: some failure"));
    }

    @Test
    void changesToFailedStatus() {
        withAdbInitializing();

        createPresenterNotTrackingView();

        onAdbFailure("some failure");
        verify(view).showWaitingStatus(contains("Failed to initialize ADB: some failure"));
    }

    @Test
    void changesToInitializedStatus() {
        withAdbInitializing();

        createPresenterNotTrackingView();

        onAdbInitComplete();
        verify(view).showWaitingStatus(contains("Waiting"));
    }

    private void createPresenterNotTrackingView() {
        createPresenter();
        reset(view);
    }

    @Test
    void openedDataSourceReplacesWaiting() {
        withAdbInitialized();

        createPresenterNotTrackingView();

        onDataSourceOpened("new DS");

        verify(view).showSourceStatus("new DS");
    }

    @Test
    void openedDataSourceReplacesFailed() {
        withAdbFailedToInitialize("failure");

        createPresenterNotTrackingView();

        onDataSourceOpened("new DS");

        verify(view).showSourceStatus("new DS");
    }

    @Test
    void openedDataSourceReplacesInit() {
        withAdbInitializing();

        createPresenterNotTrackingView();

        onDataSourceOpened("new DS");

        verify(view).showSourceStatus("new DS");
    }

    @Test
    void openedDataSourceReplacesPrevious() {
        withDataSource("old DS");

        createPresenterNotTrackingView();

        onDataSourceOpened("new DS");

        verify(view).showSourceStatus("new DS");
    }

    private void createPresenter() {
        var presenter =
                new SourceStatusPresenter(dataSourceHolder, adbServicesStatus, view, popupMenuPresenter, uiScheduler);
        presenter.init();
    }


    private DataSource withDataSource(String name) {
        var dataSource = mock(DataSource.class);
        when(dataSource.toString()).thenReturn(name);
        when(dataSourceHolder.getDataSource()).thenReturn(dataSource);
        return dataSource;
    }

    private void withAdbInitializing() {
        when(adbServicesStatus.getStatus()).thenReturn(AdbServicesStatus.StatusValue.initializing());
    }

    private void withAdbInitialized() {
        when(adbServicesStatus.getStatus()).thenReturn(AdbServicesStatus.StatusValue.initialized());
    }

    private void withAdbFailedToInitialize(String failure) {
        when(adbServicesStatus.getStatus()).thenReturn(AdbServicesStatus.StatusValue.failed(failure));
    }

    private void onAdbInitComplete() {
        withAdbInitialized();
        for (AdbServicesStatus.Observer obs : adbServicesStatusObservers) {
            obs.onAdbServicesStatusChanged(AdbServicesStatus.StatusValue.initialized());
        }
    }

    private void onAdbFailure(String failure) {
        withAdbFailedToInitialize(failure);
        for (AdbServicesStatus.Observer obs : adbServicesStatusObservers) {
            obs.onAdbServicesStatusChanged(AdbServicesStatus.StatusValue.failed(failure));
        }
    }

    private void onDataSourceOpened(String name) {
        var newDataSource = withDataSource(name);

        for (DataSourceHolder.Observer obs : dataSourceHolderObservers) {
            obs.onDataSourceChanged(null, newDataSource);
        }
    }
}

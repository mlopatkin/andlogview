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

package name.mlopatkin.andlogview.liblogcat.ddmlib;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.ui.mainframe.ErrorDialogs;

import com.google.common.util.concurrent.MoreExecutors;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeviceDisconnectedHandlerTest {
    @Mock
    DeviceDisconnectedHandler.DeviceAwaiter deviceAwaiter;

    @Mock
    ErrorDialogs errorDialogs;

    @Mock
    AdbConfigurationPref adbConfigurationPref;

    @ParameterizedTest
    @EnumSource(AdbDataSource.InvalidationReason.class)
    void showsErrorMessageWhenDeviceDisconnectsAndAutoReconnectDisabled(AdbDataSource.InvalidationReason reason) {
        when(adbConfigurationPref.isAutoReconnectEnabled()).thenReturn(false);

        var handler = createHandler();
        handler.onDataSourceInvalidated(reason);

        assertErrorDialogShown();
    }

    @ParameterizedTest
    @EnumSource(AdbDataSource.InvalidationReason.class)
    void waitsForDeviceWhenDeviceDisconnectsAndAutoReconnectEnabled(AdbDataSource.InvalidationReason reason) {
        when(adbConfigurationPref.isAutoReconnectEnabled()).thenReturn(true);

        var handler = createHandler();
        handler.onDataSourceInvalidated(reason);

        assertWaitForDevice();
    }

    @ParameterizedTest
    @EnumSource(AdbDataSource.InvalidationReason.class)
    void errorMessagesCanBeSuppressed(AdbDataSource.InvalidationReason reason) {
        when(adbConfigurationPref.isAutoReconnectEnabled()).thenReturn(false);

        var handler = createHandler();
        handler.suppressDialogs();
        handler.onDataSourceInvalidated(reason);

        assertNoErrorDialogShown();
    }

    @ParameterizedTest
    @EnumSource(AdbDataSource.InvalidationReason.class)
    void errorMessagesAreShownAfterResuming(AdbDataSource.InvalidationReason reason) {
        when(adbConfigurationPref.isAutoReconnectEnabled()).thenReturn(false);

        var handler = createHandler();
        handler.suppressDialogs();
        handler.resumeDialogs();
        handler.onDataSourceInvalidated(reason);

        assertErrorDialogShown();
    }

    @ParameterizedTest
    @EnumSource(AdbDataSource.InvalidationReason.class)
    void suppressedErrorMessagesAreShownAfterResuming(AdbDataSource.InvalidationReason reason) {
        when(adbConfigurationPref.isAutoReconnectEnabled()).thenReturn(false);

        var handler = createHandler();
        handler.suppressDialogs();
        handler.onDataSourceInvalidated(reason);
        handler.resumeDialogs();

        assertErrorDialogShown();
    }

    private void assertNoErrorDialogShown() {
        verify(errorDialogs, never()).showDeviceDisconnectedWarning(any());
        verifyNoInteractions(deviceAwaiter);
    }

    private void assertErrorDialogShown() {
        verify(errorDialogs).showDeviceDisconnectedWarning(any());
        verifyNoInteractions(deviceAwaiter);
    }

    private void assertWaitForDevice() {
        verifyNoInteractions(errorDialogs);
        verify(deviceAwaiter).waitForDevice();
    }

    private DeviceDisconnectedHandler createHandler() {
        return new DeviceDisconnectedHandler(deviceAwaiter, errorDialogs, adbConfigurationPref,
                MoreExecutors.directExecutor());
    }
}

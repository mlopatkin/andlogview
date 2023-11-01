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

package name.mlopatkin.andlogview.ui.device;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.device.AdbException;
import name.mlopatkin.andlogview.device.AdbManager;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.ui.mainframe.ErrorDialogs;
import name.mlopatkin.andlogview.utils.LazyInstance;

import com.google.common.util.concurrent.MoreExecutors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ExtendWith(MockitoExtension.class)
class AdbServicesBridgeTest {

    @Mock
    AdbManager adbManager;
    @Mock
    AdbConfigurationPref adbConfigurationPref;

    @Mock
    AdbServicesSubcomponent.Factory adbServicesFactory;

    @Mock
    ErrorDialogs errorDialogs;

    @Test
    void adbServicesCanBeObtained() {
        var bridge = createBridge();

        assertThat(bridge.getAdbServicesAsync()).isCompleted();
    }

    @Test
    void noErrorDialogShownWhenServerStartsNormally() throws Exception {
        var bridge = createBridge();

        ensureCompleted(bridge.getAdbServicesAsync());
        verify(errorDialogs, never()).showAdbNotFoundError();
    }

    @Test
    void adbServicesCannotBeObtainedIfCreatingServerFails() throws Exception {
        whenServerFailsToStart();
        var bridge = createBridge();

        assertThat(bridge.getAdbServicesAsync()).isCompletedExceptionally();
    }

    @Test
    void errorDialogIsShownWhenServerCannotBeCreated() throws Exception {
        whenServerFailsToStart();
        var bridge = createBridge();

        ensureCompleted(bridge.getAdbServicesAsync());
        verify(errorDialogs).showAdbNotFoundError();
    }

    @Test
    void theSameFutureIsAlwaysReturned() {
        var bridge = createBridge();

        assertThat(bridge.getAdbServicesAsync()).isSameAs(bridge.getAdbServicesAsync());
    }

    private AdbServicesBridge createBridge() {
        return new AdbServicesBridge(adbManager, adbConfigurationPref, adbServicesFactory,
                LazyInstance.lazy(() -> errorDialogs), MoreExecutors.directExecutor(), MoreExecutors.directExecutor());
    }


    private void whenServerFailsToStart() throws AdbException {
        when(adbManager.startServer()).thenThrow(new AdbException("Failed to create server"));
    }

    private static void ensureCompleted(Future<?> f) {
        try {
            f.get(10, TimeUnit.SECONDS);
        } catch (ExecutionException ignored) {
            // ignored
        } catch (TimeoutException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.preferences;

import static name.mlopatkin.andlogview.utils.MyFutures.errorHandler;
import static name.mlopatkin.andlogview.utils.MyFutures.ignoreCancellations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.base.concurrent.TestSequentialExecutor;
import name.mlopatkin.andlogview.config.FakeInMemoryConfigStorage;
import name.mlopatkin.andlogview.device.AdbException;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.preferences.ThemePref;
import name.mlopatkin.andlogview.ui.device.AdbOpener;
import name.mlopatkin.andlogview.ui.device.AdbServices;
import name.mlopatkin.andlogview.ui.device.AdbServicesBridge;
import name.mlopatkin.andlogview.ui.device.AdbServicesInitializationPresenter;
import name.mlopatkin.andlogview.ui.device.GlobalAdbDeviceList;
import name.mlopatkin.andlogview.utils.FakePathResolver;

import com.google.common.util.concurrent.MoreExecutors;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Integration tests for ADB error handling that involve multiple moving parts.
 */
class AdbErrorHandlingIntegTest {

    @Test
    @Issue("https://github.com/mlopatkin/andlogview/issues/491")
    void noSecondConfigurationDialogOpensIfAdbErrorOccursWhileConfigurationDialogIsOnScreen() {
        var uiExecutor = new TestSequentialExecutor(MoreExecutors.directExecutor());

        var storage = new FakeInMemoryConfigStorage();
        var adbPref = new AdbConfigurationPref(storage, FakePathResolver.acceptsAnything());
        adbPref.trySetAdbLocation("/usr/bin/adb");
        var themePref = new ThemePref(storage);

        AdbServicesBridge bridge = mock();
        CompletableFuture<AdbServices> result = new CompletableFuture<>();
        when(bridge.getAdbServicesAsync()).thenReturn(result);
        when(bridge.prepareAdbDeviceList(any())).then(args -> {
                    Consumer<Throwable> th = args.getArgument(0);
                    var ignored = result.handle(errorHandler(ignoreCancellations(th)));
                    return new GlobalAdbDeviceList(uiExecutor);
                }
        );

        AdbServicesInitializationPresenter.View adbInitView = mock();
        ConfigurationDialogPresenter.View configurationView = mock();
        var inOrder = Mockito.inOrder(adbInitView, configurationView);

        var adbInitPresenter = new AdbServicesInitializationPresenter(
                adbInitView,
                bridge,
                adbPref,
                uiExecutor,
                mock()
        );

        var configurationDialogPresenter = new ConfigurationDialogPresenter(
                configurationView,
                themePref,
                mock(),
                adbPref,
                adbInitPresenter,
                bridge,
                mock(),
                uiExecutor
        );

        var adbOpener = new AdbOpener(adbInitPresenter, mock(), uiExecutor);

        var ignored = adbOpener.awaitDevice();

        configurationDialogPresenter.openDialog();

        result.completeExceptionally(new AdbException("Simulated failure"));

        inOrder.verify(configurationView).show();
        inOrder.verify(adbInitView, never()).showAdbLoadingError(any(), anyBoolean());
        inOrder.verifyNoMoreInteractions();
    }
}

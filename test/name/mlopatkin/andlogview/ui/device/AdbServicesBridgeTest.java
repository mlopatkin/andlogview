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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.base.concurrent.TestExecutor;
import name.mlopatkin.andlogview.device.AdbException;
import name.mlopatkin.andlogview.device.AdbManager;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.ui.device.AdbServicesStatus.StatusValue;
import name.mlopatkin.andlogview.ui.mainframe.ErrorDialogs;
import name.mlopatkin.andlogview.utils.LazyInstance;
import name.mlopatkin.andlogview.utils.MyFutures;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.MoreExecutors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

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

        withEmptyUncaughtExceptionHandler(
                () -> assertThat(bridge.getAdbServicesAsync()).isCompletedExceptionally());
    }

    @Test
    void errorDialogIsShownWhenServerCannotBeCreated() throws Exception {
        whenServerFailsToStart();
        var bridge = createBridge();

        withEmptyUncaughtExceptionHandler(() -> {
            ensureCompleted(bridge.getAdbServicesAsync());
            verify(errorDialogs).showAdbNotFoundError();
        });
    }

    @Test
    void theSameFutureIsAlwaysReturned() {
        var bridge = createBridge();

        assertThat(bridge.getAdbServicesAsync()).isSameAs(bridge.getAdbServicesAsync());
    }

    @Test
    void statusIsUpdatedWhenNotificationIsSent() {
        var testUiExecutor = new TestExecutor();
        var testAdbExecutor = new TestExecutor();

        var bridge = createBridge(testUiExecutor, testAdbExecutor);

        var map = Multimaps.<StatusValue, StatusValue>newListMultimap(new LinkedHashMap<>(), ArrayList::new);

        bridge.asObservable().addObserver(newStatus -> map.put(newStatus, bridge.getStatus()));

        var servicesFuture = bridge.getAdbServicesAsync();

        drainExecutors(testUiExecutor, testAdbExecutor);

        assertThat(servicesFuture).isCompleted();
        assertThat(new ArrayList<>(map.keySet())).containsExactly(
                StatusValue.initializing(),
                StatusValue.initialized());
        assertThat(map.asMap()).allSatisfy((k, values) -> assertThat(values).singleElement().isEqualTo(k));
    }

    @Test
    void statusUpdateAreCorrectWhenDirectExecutorIsUsed() {
        var bridge = createBridge();
        var map = Multimaps.<StatusValue, StatusValue>newListMultimap(new LinkedHashMap<>(), ArrayList::new);

        bridge.asObservable().addObserver(newStatus -> map.put(newStatus, bridge.getStatus()));

        ensureCompleted(bridge.getAdbServicesAsync());

        assertThat(new ArrayList<>(map.keySet())).containsExactly(StatusValue.initialized());
        assertThat(map.asMap()).allSatisfy((k, values) -> assertThat(values).singleElement().isEqualTo(k));
    }

    @Test
    void listenerFailuresArePropagated() throws Exception {
        class MyException extends RuntimeException {}

        var testUiExecutor = new TestExecutor();
        var testAdbExecutor = new TestExecutor();

        var bridge = createBridge();
        var uncaughtHandler = mock(Thread.UncaughtExceptionHandler.class);
        var observer = mock(AdbServicesStatus.Observer.class);
        doThrow(new MyException()).when(observer).onAdbServicesStatusChanged(any());

        bridge.asObservable().addObserver(observer);

        withUncaughtExceptionHandler(uncaughtHandler, () -> {
            @SuppressWarnings("unused")
            var unused = bridge.getAdbServicesAsync();
            drainExecutors(testUiExecutor, testAdbExecutor);

            assertThat(unused).describedAs("Listener failures should not affect ADB initialization").isCompleted();
        });

        verify(uncaughtHandler).uncaughtException(any(), isA(MyException.class));
    }

    @Test
    void asyncHandlersSeeProperStatus() {
        var testUiExecutor = new TestExecutor();
        var testAdbExecutor = new TestExecutor();
        var bridge = createBridge(testUiExecutor, testAdbExecutor);

        var bridgeStatus = new AtomicReference<StatusValue>();
        var result = bridge.getAdbServicesAsync()
                .thenAcceptAsync(services -> bridgeStatus.set(bridge.getStatus()), MoreExecutors.directExecutor());

        drainExecutors(testAdbExecutor, testUiExecutor);

        assertThat(result).isCompleted();
        assertThat(bridgeStatus.get()).isEqualTo(StatusValue.initialized());
    }

    private AdbServicesBridge createBridge() {
        return createBridge(MoreExecutors.directExecutor(), MoreExecutors.directExecutor());
    }

    private AdbServicesBridge createBridge(Executor uiExecutor, Executor adbExecutor) {
        return new AdbServicesBridge(adbManager, adbConfigurationPref, adbServicesFactory,
                LazyInstance.lazy(() -> errorDialogs), uiExecutor, adbExecutor);
    }

    private void drainExecutors(TestExecutor... executor) {
        var executors = Arrays.asList(executor);
        while (Iterables.any(executors, TestExecutor::flush)) {
            // intentionally empty
        }
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

    private static void withUncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler,
            MyFutures.ThrowingRunnable r) throws Exception {
        var prevHandler = Thread.currentThread().getUncaughtExceptionHandler();
        Thread.currentThread().setUncaughtExceptionHandler(handler);
        try {
            r.run();
        } finally {
            Thread.currentThread().setUncaughtExceptionHandler(prevHandler);
        }
    }

    private static void withEmptyUncaughtExceptionHandler(MyFutures.ThrowingRunnable r) throws Exception {
        withUncaughtExceptionHandler((t, e) -> {}, r);
    }
}

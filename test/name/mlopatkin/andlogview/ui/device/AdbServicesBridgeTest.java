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

import static name.mlopatkin.andlogview.base.concurrent.ExtendedCompletableFutureAssert.assertThatCompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.base.concurrent.TestExecutor;
import name.mlopatkin.andlogview.base.concurrent.TestSequentialExecutor;
import name.mlopatkin.andlogview.device.AdbException;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.test.ThreadTestUtils;
import name.mlopatkin.andlogview.ui.device.AdbServicesStatus.StatusValue;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.MoreExecutors;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@ExtendWith(MockitoExtension.class)
class AdbServicesBridgeTest {
    @Mock
    AdbServiceStarter adbStarter;
    @Mock
    AdbConfigurationPref adbConfigurationPref;

    @Mock
    AdbServicesSubcomponent.Factory adbServicesFactory;

    @Test
    void adbServicesCanBeObtained() {
        var bridge = createBridge();

        assertThat(bridge.getAdbServicesAsync()).isCompleted();
    }

    @Test
    void adbServicesCannotBeObtainedIfCreatingServerFails() throws Exception {
        whenServerFailsToStart();
        var bridge = createBridge();

        ThreadTestUtils.withEmptyUncaughtExceptionHandler(
                () -> assertThat(bridge.getAdbServicesAsync()).isCompletedExceptionally());
    }

    @Test
    void returnedFutureCanBeCancelled() {
        var testUiExecutor = new TestExecutor();
        var testAdbExecutor = new TestExecutor();

        var bridge = createBridge(testUiExecutor, testAdbExecutor);

        var future = bridge.getAdbServicesAsync();

        assertThat(future.cancel(false)).isTrue();
        assertThat(future).isCancelled();
    }

    @Test
    void furtherFuturesAreNotCancelledByCancellingTheFirstOne() {
        var testUiExecutor = new TestExecutor();
        var testAdbExecutor = new TestExecutor();

        var bridge = createBridge(testUiExecutor, testAdbExecutor);

        var future = bridge.getAdbServicesAsync();
        future.cancel(false);

        assertThat(bridge.getAdbServicesAsync()).isNotCancelled();
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

        ThreadTestUtils.withUncaughtExceptionHandler(uncaughtHandler, () -> {
            @SuppressWarnings("unused")
            var unused = bridge.getAdbServicesAsync();
            drainExecutors(testUiExecutor, testAdbExecutor);

            assertThat(unused).describedAs("Listener failures should not affect ADB initialization").isCompleted();
        });

        verify(uncaughtHandler).uncaughtException(any(), isA(CompletionException.class));
    }

    @Test
    void asyncHandlersSeeProperStatus() {
        var testUiExecutor = new TestExecutor();
        var testAdbExecutor = new TestExecutor();
        var bridge = createBridge(testUiExecutor, testAdbExecutor);

        AtomicReference<@Nullable StatusValue> bridgeStatus = new AtomicReference<>();
        var result = bridge.getAdbServicesAsync()
                .thenAcceptAsync(services -> bridgeStatus.set(bridge.getStatus()), MoreExecutors.directExecutor());

        drainExecutors(testAdbExecutor, testUiExecutor);

        assertThat(result).isCompleted();
        assertThat(bridgeStatus.get()).isEqualTo(StatusValue.initialized());
    }

    @Test
    void serviceFutureIsCancelledIfAdbStopped() {
        var testUiExecutor = new TestExecutor();
        var testAdbExecutor = new TestExecutor();
        var bridge = createBridge(testUiExecutor, testAdbExecutor);

        var serviceFuture = bridge.getAdbServicesAsync();
        bridge.stopAdb();

        assertThatCompletableFuture(serviceFuture).isCancelledUpstream();
    }

    @Test
    void adbIsNotInitializedAfterStopped() {
        var testUiExecutor = new TestExecutor();
        var testAdbExecutor = new TestExecutor();
        var bridge = createBridge(testUiExecutor, testAdbExecutor);
        var observer = mock(AdbServicesStatus.Observer.class);

        var firstInit = bridge.getAdbServicesAsync();
        bridge.stopAdb();
        bridge.asObservable().addObserver(observer);
        drainExecutors(testAdbExecutor, testUiExecutor);

        assertThat(bridge.getStatus()).isInstanceOf(AdbServicesStatus.NotInitialized.class);

        assertThat(firstInit).isCompletedExceptionally();
        verifyNoInteractions(observer);
    }

    @Test
    void completingPreviousAdbInitializationAfterStopDoesNotChangeStatus() {
        var testUiExecutor = new TestExecutor();
        var testAdbExecutor = new TestExecutor();
        var bridge = createBridge(testUiExecutor, testAdbExecutor);
        var observer = mock(AdbServicesStatus.Observer.class);

        var firstInit = bridge.getAdbServicesAsync();
        testAdbExecutor.flush();
        bridge.stopAdb();
        bridge.asObservable().addObserver(observer);
        drainExecutors(testAdbExecutor, testUiExecutor);

        assertThat(firstInit).isCompletedExceptionally();
        assertThat(bridge.getStatus()).isInstanceOf(AdbServicesStatus.NotInitialized.class);
        verifyNoInteractions(observer);
    }

    @Test
    void completingNewAdbInitializationAfterStopSendsNoSpuriousNotifications() {
        var testUiExecutor = new TestExecutor();
        var testAdbExecutor = new TestExecutor();
        var bridge = createBridge(testUiExecutor, testAdbExecutor);
        var observer = mock(AdbServicesStatus.Observer.class);

        var firstInit = bridge.getAdbServicesAsync();
        bridge.stopAdb();
        var secondInit = bridge.getAdbServicesAsync();
        bridge.asObservable().addObserver(observer);
        drainExecutors(testAdbExecutor, testUiExecutor);

        assertThat(firstInit).isCompletedExceptionally();
        assertThat(secondInit).isCompleted().isNotCancelled();

        verify(observer).onAdbServicesStatusChanged(isA(AdbServicesStatus.Initialized.class));
        verifyNoMoreInteractions(observer);
    }

    @Test
    void cleansUpGlobalAdbListWhenStoppingAdb() throws Exception {
        GlobalAdbDeviceList deviceList = mock();
        var bridge = createBridge(deviceList);

        assertThat(bridge.getAdbServicesAsync()).isCompleted();

        var order = inOrder(deviceList, adbStarter);
        order.verify(adbStarter).startAdb();  // Initial start
        order.verify(deviceList).setAdbServer(any());

        bridge.stopAdb();

        order.verify(deviceList).setAdbServer(isNull());
        order.verifyNoMoreInteractions();
    }

    @Test
    void enablesErrorDialogsUponSuccessfulAdbInit() {
        var bridge = createBridge();

        assertThat(bridge.getAdbServicesAsync()).isCompleted();

        verify(adbConfigurationPref).setShowAdbAutostartFailures(true);
    }

    @Test
    void doesNotEnableErrorDialogsUponSuccessfulAdbInit() throws Exception {
        whenServerFailsToStart();

        var bridge = createBridge();

        assertThat(bridge.getAdbServicesAsync()).isCompletedExceptionally();

        verify(adbConfigurationPref, never()).setShowAdbAutostartFailures(true);
    }

    private AdbServicesBridge createBridge() {
        return createBridge(MoreExecutors.directExecutor(), MoreExecutors.directExecutor());
    }

    private AdbServicesBridge createBridge(GlobalAdbDeviceList deviceList) {
        return new AdbServicesBridge(
                adbStarter,
                adbConfigurationPref,
                adbServicesFactory,
                deviceList,
                new TestSequentialExecutor(MoreExecutors.directExecutor()),
                MoreExecutors.directExecutor()
        );
    }

    private AdbServicesBridge createBridge(Executor uiExecutor, Executor adbExecutor) {
        var sequentialUiExecutor = new TestSequentialExecutor(uiExecutor);
        return new AdbServicesBridge(
                adbStarter,
                adbConfigurationPref,
                adbServicesFactory,
                new GlobalAdbDeviceList(sequentialUiExecutor),
                sequentialUiExecutor,
                adbExecutor
        );
    }

    private void drainExecutors(TestExecutor... executor) {
        var executors = Arrays.asList(executor);
        while (Iterables.any(executors, TestExecutor::flush)) {
            // intentionally empty
        }
    }

    private void whenServerFailsToStart() throws AdbException {
        when(adbStarter.startAdb()).thenThrow(new AdbException("Failed to create server"));
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

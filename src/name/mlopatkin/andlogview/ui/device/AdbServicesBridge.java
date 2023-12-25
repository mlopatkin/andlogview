/*
 * Copyright 2022 Mikhail Lopatkin
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

import static name.mlopatkin.andlogview.utils.MyFutures.consumingHandler;
import static name.mlopatkin.andlogview.utils.MyFutures.errorHandler;
import static name.mlopatkin.andlogview.utils.MyFutures.ignoreCancellations;
import static name.mlopatkin.andlogview.utils.MyFutures.runAsync;

import name.mlopatkin.andlogview.AppExecutors;
import name.mlopatkin.andlogview.base.MyThrowables;
import name.mlopatkin.andlogview.device.AdbDeviceList;
import name.mlopatkin.andlogview.device.AdbException;
import name.mlopatkin.andlogview.device.AdbManager;
import name.mlopatkin.andlogview.device.AdbServer;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.utils.MyFutures;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * This class is responsible for managing the ADB-dependent sub-graph of dependencies. It exposes various ADB-related
 * services to the rest of the application.
 */
@MainFrameScoped
public class AdbServicesBridge implements AdbServicesStatus {
    // Why does this class live in the ui layer? So far everything ADB-related goes through UI layer be it device dump,
    // device selection or the actual logcat retrieval. Moreover, ADB initialization code shows error dialogs if
    // something goes wrong, so it has some UI dependency already. If this becomes problematic then the class may be
    // split into two in the future.
    private static final Logger logger = Logger.getLogger(AdbServicesBridge.class);

    private final AdbManager adbManager;
    private final AdbConfigurationPref adbConfigurationPref;
    private final AdbServicesSubcomponent.Factory adbSubcomponentFactory;
    private final Executor uiExecutor;
    private final Executor adbInitExecutor;
    private final GlobalAdbDeviceList adbDeviceList;
    private final Subject<Observer> statusObservers = new Subject<>();

    private @Nullable CompletableFuture<AdbServices> adbSubcomponent; // This one is three-state. The `null` here
    // means that nobody attempted to initialize ADB yet. Non-null holds the current subcomponent if it was created
    // successfully or the initialization error if something failed.

    @Inject
    AdbServicesBridge(AdbManager adbManager,
            AdbConfigurationPref adbConfigurationPref,
            AdbServicesSubcomponent.Factory adbSubcomponentFactory,
            @Named(AppExecutors.UI_EXECUTOR) Executor uiExecutor,
            @Named(AppExecutors.FILE_EXECUTOR) Executor adbInitExecutor) {
        this.adbManager = adbManager;
        this.adbConfigurationPref = adbConfigurationPref;
        this.adbSubcomponentFactory = adbSubcomponentFactory;
        this.uiExecutor = uiExecutor;
        this.adbInitExecutor = adbInitExecutor;
        this.adbDeviceList = new GlobalAdbDeviceList(uiExecutor);
    }

    /**
     * Tries to create AdbServices, potentially initializing ADB connection if is it is not ready yet.
     * This may fail, or may be cancelled.
     *
     * @return a completable future that will provide {@link AdbServices} when ready
     */
    public CompletableFuture<AdbServices> getAdbServicesAsync() {
        var result = adbSubcomponent;
        if (result == null) {
            result = initAdbAsync();
        }
        // Prevent clients from cancelling the whole chain. Java 8-safe version of CompletableFuture.copy.
        return result.thenApply(Function.identity());
    }

    private CompletableFuture<AdbServices> initAdbAsync() {
        assert adbSubcomponent == null;
        Stopwatch stopwatch = Stopwatch.createStarted();

        final var result = adbSubcomponent =
                runAsync(() -> adbManager.startServer(adbConfigurationPref), adbInitExecutor)
                        .thenApplyAsync(this::buildServices, uiExecutor);

        if (!result.isDone()) {
            // This happens always unless direct executors are used.
            // It is important to have adbSubcomponent initialized before, or getStatus() inside listeners would return
            // outdated value.
            notifyStatusChange(getStatus());
        }

        // This is a separate chain, not related to the consumers of getAdbServicesAsync. Therefore, it has a separate
        // exception sink to handle runtime errors in the handler.
        result.handleAsync(
                        consumingHandler((r, th) -> onAdbInitFinished(result, th, stopwatch)),
                        uiExecutor)
                .exceptionally(MyFutures::uncaughtException);
        return result;
    }

    private AdbServices buildServices(AdbServer adbServer) {
        adbDeviceList.setAdbServer(adbServer);
        return adbSubcomponentFactory.build(adbDeviceList);
    }

    private void onAdbInitFinished(CompletableFuture<?> origin, @Nullable Throwable maybeFailure,
            Stopwatch timeTracing) {
        if (adbSubcomponent != origin) {
            // Our initialization was cancelled by this point. We shouldn't propagate notifications further.
            return;
        }
        logger.info("Initialized adb server in " + timeTracing.elapsed(TimeUnit.MILLISECONDS) + "ms");
        if (maybeFailure != null) {
            logger.error("Failed to initialize ADB", maybeFailure);
            notifyStatusChange(StatusValue.failed(getAdbFailureString(maybeFailure)));
        } else {
            notifyStatusChange(StatusValue.initialized());
        }
    }

    public AdbDeviceList prepareAdbDeviceList(Consumer<? super Throwable> adbInitFailureHandler) {
        // Kick off ADB initialization if not already
        getAdbServicesAsync().handle(errorHandler(ignoreCancellations(adbInitFailureHandler)))
                .exceptionally(MyFutures::uncaughtException);
        return adbDeviceList;
    }

    @Override
    public StatusValue getStatus() {
        var services = adbSubcomponent;
        if (services == null) {
            return StatusValue.notInitialized();
        }

        try {
            return services.<StatusValue>thenApply(unused -> StatusValue.initialized())
                    .getNow(StatusValue.initializing());
        } catch (CompletionException e) {
            return StatusValue.failed(getAdbFailureString(e));
        }
    }

    @Override
    public Observable<Observer> asObservable() {
        return statusObservers.asObservable();
    }

    private void notifyStatusChange(StatusValue newStatus) {
        for (Observer observer : statusObservers) {
            observer.onAdbServicesStatusChanged(newStatus);
        }
    }

    private static String getAdbFailureString(Throwable th) {
        return MyThrowables.findCause(th, AdbException.class)
                .map(Throwable::getMessage)
                .orElse(MoreObjects.firstNonNull(th.getMessage(), "unknown failure"));
    }

    /**
     * Discards the running adb services subcomponent if any. Any ongoing ADB initialization is cancelled.
     */
    public void stopAdb() {
        var currentAdb = adbSubcomponent;
        if (currentAdb != null) {
            adbSubcomponent = null;
            notifyStatusChange(getStatus());
            currentAdb.cancel(false);
        }
    }
}

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

import name.mlopatkin.andlogview.device.AdbException;
import name.mlopatkin.andlogview.device.AdbManager;
import name.mlopatkin.andlogview.device.AdbServer;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.ui.mainframe.ErrorDialogs;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.utils.MyFutures;
import name.mlopatkin.andlogview.utils.Optionals;
import name.mlopatkin.andlogview.utils.Try;

import com.google.common.base.Stopwatch;

import dagger.Lazy;

import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * This class is responsible for managing the ADB-dependent sub-graph of dependencies. It exposes various ADB-related
 * services to the rest of the application.
 */
@MainFrameScoped
public class AdbServicesBridge {
    // Why does this class live in the ui layer? So far everything ADB-related goes through UI layer be it device dump,
    // device selection or the actual logcat retrieval. Moreover, ADB initialization code shows error dialogs if
    // something goes wrong, so it has some UI dependency already. If this becomes problematic then the class may be
    // split into two in the future.
    private static final Logger logger = Logger.getLogger(AdbServicesBridge.class);

    private final AdbManager adbManager;
    private final AdbConfigurationPref adbConfigurationPref;
    private final AdbServicesSubcomponent.Factory adbSubcomponentFactory;
    private final Lazy<ErrorDialogs> errorDialogs;

    // TODO(mlopatkin) My attempt to hide connection replacement logic in the AdbServer failed. What if the connection
    //  update fails? The server would be unusable and we have to discard the whole component.
    @Nullable
    private Try<AdbServicesSubcomponent> adbSubcomponent;  // This one is three-state. The `null` here means that nobody
    // attempted to initialize ADB yet. Non-null holds the current subcomponent if it was created successfully or the
    // initialization error if something failed. I could get away with nullable Optional, I guess, but Optional fields
    // are not recommended and nullable Optionals are a recipe for disaster anyway.

    @Inject
    AdbServicesBridge(AdbManager adbManager,
            AdbConfigurationPref adbConfigurationPref,
            AdbServicesSubcomponent.Factory adbSubcomponentFactory,
            Lazy<ErrorDialogs> errorDialogs) {
        this.adbManager = adbManager;
        this.adbConfigurationPref = adbConfigurationPref;
        this.adbSubcomponentFactory = adbSubcomponentFactory;
        this.errorDialogs = errorDialogs;
    }

    private Optional<AdbServices> getAdbServices() {
        Try<AdbServicesSubcomponent> result = adbSubcomponent;
        if (result == null) {
            result = adbSubcomponent = Try.ofCallable(this::tryCreateAdbServer).map(adbSubcomponentFactory::build);
        }
        return Optionals.upcast(result.toOptional());
    }

    /**
     * Tries to create AdbServices, potentially initializing ADB connection if is it is not ready yet.
     * This may fail and show an error dialog.
     *
     * @return a completable future that will provide {@link AdbServices} when ready
     */
    public CompletableFuture<AdbServices> getAdbServicesAsync() {
        return getAdbServices().map(CompletableFuture::completedFuture)
                .orElseGet(() -> MyFutures.failedFuture(new AdbException("Placeholder")));
    }

    private AdbServer tryCreateAdbServer() throws AdbException {
        // TODO(mlopatkin) ADB init takes quite some time, we should do this asynchronously.
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            adbManager.setAdbLocation(adbConfigurationPref);
            return adbManager.startServer();
        } catch (AdbException e) {
            logger.error("Failed to initialize ADB", e);
            // TODO(mlopatkin) should we show dialog from here or should we move this responsibility somewhere else?
            errorDialogs.get().showAdbNotFoundError();
            throw e;
        } finally {
            logger.info("Initialized adb server in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        }

    }
}

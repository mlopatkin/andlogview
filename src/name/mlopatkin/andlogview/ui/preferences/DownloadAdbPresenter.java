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

import name.mlopatkin.andlogview.AppExecutors;
import name.mlopatkin.andlogview.sdkrepo.SdkRepository;
import name.mlopatkin.andlogview.utils.MyFutures;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * This presenter provides the full ADB installation flow.
 */
public class DownloadAdbPresenter implements InstallAdbPresenter {
    // Since the dawn of time, ADB lives there
    private static final String PLATFORM_TOOLS_PACKAGE = "platform-tools";
    private final SdkRepository repository;
    private final ExecutorService networkExecutor;

    @Inject
    DownloadAdbPresenter(SdkRepository repository, @Named(AppExecutors.FILE_EXECUTOR) ExecutorService networkExecutor) {
        this.repository = repository;
        this.networkExecutor = networkExecutor;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public CompletableFuture<Result> startInstall() {
        // 1. initialize the SdkRepository
        // 2. get the package information (may take time, cancellable)
        // 3. if present - prompt the user to accept the license and configure the download path (cancellable)
        // 3fail. if not present - fall back to opening the browser
        // 4. Download the archive (may take time, cancellable).
        // 5. Unpack the archive (may take time, cancellable).
        // 6. Give the installed ADB path back.

        var packageData =
                MyFutures.runAsync(() -> repository.locatePackage(PLATFORM_TOOLS_PACKAGE), networkExecutor);

        return packageData.thenApply(
                data -> data.map(r -> (Result) Result.installed(new File("adb"))).orElse(Result.notFound())
        ).exceptionally(Result::failure);
    }
}

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
import name.mlopatkin.andlogview.features.Features;

import com.google.common.annotations.VisibleForTesting;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * Manages the creation of the presenter.
 */
public class InstallAdbPresenterFactory {
    private final boolean downloadAdbEnabled;
    private final Provider<DesktopInstallAdbPresenter> desktopInstalProvider;
    private final Provider<DownloadAdbPresenter> downloadProvider;
    private final Executor uiExecutor;

    @Inject
    public InstallAdbPresenterFactory(
            Features features,
            Provider<DesktopInstallAdbPresenter> desktopInstalProvider,
            Provider<DownloadAdbPresenter> downloadProvider,
            @Named(AppExecutors.UI_EXECUTOR) Executor uiExecutor
    ) {
        this(features.downloadAdb.isEnabled(), desktopInstalProvider, downloadProvider, uiExecutor);
    }

    @VisibleForTesting
    InstallAdbPresenterFactory(
            boolean downloadAdbEnabled,
            Provider<DesktopInstallAdbPresenter> desktopInstalProvider,
            Provider<DownloadAdbPresenter> downloadProvider,
            Executor uiExecutor
    ) {
        this.downloadAdbEnabled = downloadAdbEnabled;
        this.desktopInstalProvider = desktopInstalProvider;
        this.downloadProvider = downloadProvider;
        this.uiExecutor = uiExecutor;
    }

    public InstallAdbPresenter createPresenter() {
        if (!downloadAdbEnabled) {
            return DisabledInstallAdbPresenter.INSTANCE;
        }

        return new CompoundPresenter();
    }

    private class CompoundPresenter implements InstallAdbPresenter {
        private final DownloadAdbPresenter downloadPresenter = downloadProvider.get();
        private final DesktopInstallAdbPresenter desktopInstallPresenter = desktopInstalProvider.get();

        @Override
        public boolean isAvailable() {
            return downloadPresenter.isAvailable() || desktopInstallPresenter.isAvailable();
        }

        @Override
        public CompletableFuture<Result> startInstall() {
            if (downloadPresenter.isAvailable()) {
                return downloadPresenter.startInstall().thenComposeAsync(result -> {
                    if (result instanceof ManualFallback || result instanceof PackageNotFound) {
                        return desktopInstallPresenter.startInstall();
                    }

                    assert result instanceof Installed
                           || result instanceof Cancelled
                           || result instanceof DownloadFailure;

                    return CompletableFuture.completedFuture(result);
                }, uiExecutor);
            }

            if (desktopInstallPresenter.isAvailable()) {
                return desktopInstallPresenter.startInstall();
            }

            return CompletableFuture.completedFuture(
                    Result.failure(new UnsupportedOperationException("Installing ADB is not available")));
        }
    }
}

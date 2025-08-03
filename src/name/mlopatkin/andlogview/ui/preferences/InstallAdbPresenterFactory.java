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

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Manages the creation of the presenter.
 */
public class InstallAdbPresenterFactory {
    private final Provider<DesktopInstallAdbPresenter> desktopInstalProvider;
    private final Provider<DownloadAdbPresenter> downloadProvider;

    @Inject
    public InstallAdbPresenterFactory(
            Provider<DesktopInstallAdbPresenter> desktopInstalProvider,
            Provider<DownloadAdbPresenter> downloadProvider
    ) {
        this.desktopInstalProvider = desktopInstalProvider;
        this.downloadProvider = downloadProvider;
    }

    public InstallAdbPresenter createPresenter() {
        return new CompoundPresenter();
    }

    private class CompoundPresenter implements InstallAdbPresenter {
        // TODO(mlopatkin) this class is a stub
        private final DownloadAdbPresenter downloadPresenter = downloadProvider.get();
        private final DesktopInstallAdbPresenter desktopInstallPresenter = desktopInstalProvider.get();

        @Override
        public boolean isAvailable() {
            return downloadPresenter.isAvailable() || desktopInstallPresenter.isAvailable();
        }

        @Override
        public CompletableFuture<Result> startInstall() {
            if (downloadPresenter.isAvailable()) {
                return downloadPresenter.startInstall().thenCompose(result ->
                        (result instanceof PackageNotFound || result instanceof DownloadFailure)
                                ? desktopInstallPresenter.startInstall()
                                : CompletableFuture.completedFuture(result)
                );
            }
            if (desktopInstallPresenter.isAvailable()) {
                return desktopInstallPresenter.startInstall();
            }

            return CompletableFuture.completedFuture(Result.failure(new UnsupportedOperationException()));
        }
    }
}

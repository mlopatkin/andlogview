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

import static name.mlopatkin.andlogview.utils.MyFutures.cancellationTransformer;
import static name.mlopatkin.andlogview.utils.MyFutures.toCancellable;

import name.mlopatkin.andlogview.AppExecutors;
import name.mlopatkin.andlogview.Main;
import name.mlopatkin.andlogview.sdkrepo.SdkPackage;
import name.mlopatkin.andlogview.sdkrepo.SdkRepository;
import name.mlopatkin.andlogview.utils.MyFutures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * This presenter provides the full ADB installation flow.
 */
public class DownloadAdbPresenter implements InstallAdbPresenter {
    private static final Logger log = LoggerFactory.getLogger(DownloadAdbPresenter.class);

    interface SdkInitView {
        void show(Runnable cancelAction);

        void hide();
    }

    interface InstallView {
        /**
         * Sets the action to be invoked when the license is accepted/rejected.
         *
         * @param acceptAction the action
         */
        void setAcceptAction(Consumer<Boolean> acceptAction);

        /**
         * Sets the license text to display to the user to accept.
         *
         * @param licenseText the text of the license
         */
        void setLicenseText(String licenseText);

        /**
         * Sets the installation location to be displayed in the UI. The user may alter it.
         *
         * @param path the path to install ADB into
         */
        void setInstallLocation(File path);

        /**
         * When user wants to select the directory visually.
         *
         * @param action the action to run
         */
        void setInstallLocationSelectionAction(Runnable action);

        /**
         * Prompts the user to select the installation directory
         *
         * @param commitAction called when the user selects a directory
         * @param cancelAction called when the user abandons directory lookup
         */
        void showInstallDirSelector(Consumer<? super File> commitAction, Runnable cancelAction);

        /**
         * Shows the dialog to the user. The dialog doesn't close itself, the result handler must call {@link #hide()}.
         */
        void show(Consumer<? super File> commitAction, Runnable cancelAction);

        /**
         * Closes the dialog.
         */
        void hide();

        /**
         * Allows the user to proceed with downloading. Typically, this means that the installation location is provided
         * and the license is accepted.
         */
        void setDownloadAllowed(boolean allowed);
    }

    // Since the dawn of time, ADB lives there
    private static final String PLATFORM_TOOLS_PACKAGE = "platform-tools";
    private final SdkRepository repository;
    private final Provider<SdkInitView> sdkInitView;
    private final Provider<InstallView> installView;
    private final Executor uiExecutor;
    private final Executor networkExecutor;

    @Inject
    DownloadAdbPresenter(
            SdkRepository repository,
            Provider<SdkInitView> sdkInitView,
            Provider<InstallView> installView,
            @Named(AppExecutors.UI_EXECUTOR) Executor uiExecutor,
            @Named(AppExecutors.FILE_EXECUTOR) Executor networkExecutor
    ) {
        this.repository = repository;
        this.sdkInitView = sdkInitView;
        this.installView = installView;
        this.uiExecutor = uiExecutor;
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

        var packageLookupCompleted = showSdkInitProgressDialog(packageData);

        return packageLookupCompleted.thenComposeAsync(pkgOpt ->
                        pkgOpt.map(this::installPackage).orElse(CompletableFuture.completedFuture(Result.notFound())))
                .exceptionally(cancellationTransformer(Result::cancelled, Result::failure));
    }

    // Shows progress dialog while sdk package list is being downloaded.
    // The user may cancel the download.
    private CompletableFuture<Optional<SdkPackage>> showSdkInitProgressDialog(
            CompletableFuture<Optional<SdkPackage>> sdkInit) {
        SdkInitView progressView = sdkInitView.get();
        var completion = sdkInit.whenCompleteAsync((r, th) -> progressView.hide(), uiExecutor);
        progressView.show(toCancellable(sdkInit)::cancel);
        return completion;
    }

    private CompletableFuture<Result> installPackage(SdkPackage pkg) {
        var installLocation = showAdbInstallDialog(pkg);
        var installedAdb = installLocation.thenComposeAsync(
                installDir -> showInstallProgressDialog(downloadAndUnpack(pkg, installDir)),
                uiExecutor
        );
        return installedAdb.thenApply(Result::installed);
    }

    private CompletableFuture<File> showAdbInstallDialog(SdkPackage pkg) {
        var result = new CompletableFuture<File>();
        var view = installView.get();
        view.setLicenseText(pkg.getLicense());
        view.setInstallLocation(new File(Main.getConfigurationDir(), "platform-tools"));
        view.setDownloadAllowed(false);
        view.setInstallLocationSelectionAction(() -> {
            view.showInstallDirSelector(view::setInstallLocation, () -> {});
        });
        view.setAcceptAction(view::setDownloadAllowed);

        view.show(installDir -> {
                    view.hide();
                    result.complete(installDir);
                },
                () -> {
                    view.hide();
                    result.cancel(false);
                }
        );
        return result;
    }

    private CompletableFuture<File> downloadAndUnpack(SdkPackage pkg, File installDir) {
        return MyFutures.runAsync(() -> repository.downloadPackage(pkg, installDir), networkExecutor)
                .whenCompleteAsync((r, th) -> {
                    if (th != null) {
                        log.error("Failed to download", th);
                    }
                })
                .thenApply(ignored -> installDir);
    }

    private CompletableFuture<File> showInstallProgressDialog(CompletableFuture<File> install) {
        var completion = install.whenCompleteAsync((r, th) -> {}, uiExecutor);
        return completion;
    }
}

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

import com.google.common.annotations.VisibleForTesting;

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

    interface SdkDownloadView {
        void show(Runnable cancelAction);

        void hide();
    }

    // Since the dawn of time, ADB lives there
    @VisibleForTesting
    static final String PLATFORM_TOOLS_PACKAGE = "platform-tools";
    private final SdkRepository repository;
    private final Provider<SdkInitView> sdkInitView;
    private final Provider<InstallView> installView;
    private final Provider<SdkDownloadView> downloadView;
    private final Executor uiExecutor;
    private final Executor networkExecutor;

    @Inject
    DownloadAdbPresenter(
            SdkRepository repository,
            Provider<SdkInitView> sdkInitView,
            Provider<InstallView> installView,
            Provider<SdkDownloadView> downloadView,
            @Named(AppExecutors.UI_EXECUTOR) Executor uiExecutor,
            @Named(AppExecutors.FILE_EXECUTOR) Executor networkExecutor
    ) {
        this.repository = repository;
        this.sdkInitView = sdkInitView;
        this.installView = installView;
        this.downloadView = downloadView;
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

        return packageLookupCompleted.thenComposeAsync(
                        pkgOpt ->
                                pkgOpt
                                        .map(this::installPackage)
                                        .orElse(CompletableFuture.completedFuture(Result.notFound())),
                        uiExecutor)
                .exceptionally(cancellationTransformer(Result::cancelled, Result::failure));
    }

    /**
     * Shows progress dialog while sdk package list is being downloaded.
     * The user may cancel the download, which will cancel the provided {@code sdkInit} future.
     * This runs on ui executor.
     *
     * @param sdkInit a cancellable future that downloads the SdkPackage from the repo
     * @return the decorated non-cancellable future that handles the dialog UI showing and hiding
     */
    private CompletableFuture<Optional<SdkPackage>> showSdkInitProgressDialog(
            CompletableFuture<Optional<SdkPackage>> sdkInit) {
        SdkInitView progressView = sdkInitView.get();
        var completion = sdkInit.whenCompleteAsync((r, th) -> progressView.hide(), uiExecutor);
        progressView.show(toCancellable(sdkInit)::cancel);
        return completion;
    }

    /**
     * Proceeds with installing the package, showing UI if necessary.
     *
     * @param pkg the ADB package data
     * @return a non-cancellable future with the result of the installation
     */
    private CompletableFuture<Result> installPackage(SdkPackage pkg) {
        var installLocation = showAdbInstallDialog(pkg);
        var installedAdb = installLocation.thenComposeAsync(
                installDir -> showInstallProgressDialog(downloadAndUnpack(pkg, installDir)),
                uiExecutor
        );
        return installedAdb.thenApply(Result::installed);
    }

    /**
     * Shows the installation dialog for the user to accept the license and select the installation location if desired.
     *
     * @param pkg the ADB package
     * @return the future with the selected installation path, will be cancelled if the user aborts install
     */
    private CompletableFuture<File> showAdbInstallDialog(SdkPackage pkg) {
        var result = new CompletableFuture<File>();
        var view = installView.get();
        view.setLicenseText(pkg.getLicense());
        view.setInstallLocation(new File(Main.getConfigurationDir(), "android-sdk"));
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

    /**
     * Starts the downloading and unpacking (asynchronously).
     * @param pkg the package to download and unpack
     * @param installDir the target installation directory
     * @return the future with the resulting unpacked location of the package. Can carry the failure information
     */
    private CompletableFuture<File> downloadAndUnpack(SdkPackage pkg, File installDir) {
        return MyFutures.runAsync(() -> repository.downloadPackage(pkg, installDir), networkExecutor)
                .whenComplete((r, th) -> {
                    if (th != null) {
                        log.error("Failed to download", th);
                    }
                })
                .thenApply(ignored -> installDir);
    }

    /**
     * Shows the progress dialog for ADB downloading and installing that can be cancelled by the user.
     *
     * @param install the installation future, will be cancelled if the user cancels the dialog
     * @return the decorated future that handles the dialog UI
     */
    private CompletableFuture<File> showInstallProgressDialog(CompletableFuture<File> install) {
        var view = downloadView.get();
        var completion = install.whenCompleteAsync((r, th) -> view.hide(), uiExecutor);
        view.show(MyFutures.toCancellable(install)::cancel);
        return completion;
    }
}

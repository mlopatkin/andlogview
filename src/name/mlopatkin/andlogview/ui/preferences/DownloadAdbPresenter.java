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
import name.mlopatkin.andlogview.config.ConfigurationLocation;
import name.mlopatkin.andlogview.sdkrepo.AdbLocationDiscovery;
import name.mlopatkin.andlogview.sdkrepo.ManifestParseException;
import name.mlopatkin.andlogview.sdkrepo.SdkException;
import name.mlopatkin.andlogview.sdkrepo.SdkPackage;
import name.mlopatkin.andlogview.sdkrepo.SdkRepository;
import name.mlopatkin.andlogview.sdkrepo.TargetDirectoryNotEmptyException;
import name.mlopatkin.andlogview.utils.MyFutures;
import name.mlopatkin.andlogview.utils.Try;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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

    interface FailureView {
        void show(String message, Throwable failure, Runnable tryAgain, Runnable installManually, Runnable cancel);
    }

    interface DirectoryWarningView {
        /**
         * Shows a warning that the selected directory is not empty and gives the user options.
         *
         * @param directory the directory that is not empty
         * @param continueAnyway called if user wants to continue with the non-empty directory
         * @param chooseAnother called if user wants to select a different directory
         * @param cancel called if user wants to cancel the installation
         */
        void show(File directory, Runnable continueAnyway, Runnable chooseAnother, Runnable cancel);
    }

    interface InstallView {
        /**
         * Sets the action to be invoked when the license is accepted/rejected.
         *
         * @param acceptAction the action
         */
        void setAcceptAction(Consumer<Boolean> acceptAction);

        /**
         * Sets the initial state of the license acceptance. It doesn't run the accept action.
         *
         * @param licenseAccepted the state of the acceptance
         */
        void setLicenseAccepted(boolean licenseAccepted);

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
    private final ConfigurationLocation configurationLocation;
    private final SdkRepository repository;
    private final Provider<SdkInitView> sdkInitView;
    private final Provider<InstallView> installView;
    private final Provider<FailureView> failureView;
    private final Provider<DirectoryWarningView> directoryWarningView;
    private final Provider<SdkDownloadView> downloadView;
    private final Executor uiExecutor;
    private final Executor networkExecutor;

    @Inject
    DownloadAdbPresenter(
            ConfigurationLocation configurationLocation,
            SdkRepository repository,
            Provider<SdkInitView> sdkInitView,
            Provider<InstallView> installView,
            Provider<FailureView> failureView,
            Provider<DirectoryWarningView> directoryWarningView,
            Provider<SdkDownloadView> downloadView,
            @Named(AppExecutors.UI_EXECUTOR) Executor uiExecutor,
            @Named(AppExecutors.FILE_EXECUTOR) Executor networkExecutor
    ) {
        this.configurationLocation = configurationLocation;
        this.repository = repository;
        this.sdkInitView = sdkInitView;
        this.installView = installView;
        this.failureView = failureView;
        this.directoryWarningView = directoryWarningView;
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
        // x. If anything goes wrong while downloading - allow the user to try again.

        CompletableFuture<Optional<SdkPackage>> packageData =
                MyFutures.runAsync(() -> {
                    try {
                        return repository.locatePackage(PLATFORM_TOOLS_PACKAGE);
                    } catch (IOException | ManifestParseException e) {
                        log.error(
                                "Failed to locate {} package, falling back to manual install",
                                PLATFORM_TOOLS_PACKAGE,
                                e
                        );
                        // For now, don't bother user with the failure.
                        return Optional.empty();
                    }
                }, networkExecutor);

        var packageLookupCompleted = showSdkInitProgressDialog(packageData);

        return packageLookupCompleted.thenComposeAsync(
                        pkgOpt ->
                                pkgOpt
                                        .map(this::installPackage)
                                        .orElse(CompletableFuture.completedFuture(Result.notFound())),
                        uiExecutor)
                .exceptionally(cancellationTransformer(Result::cancelled));
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
     * Proceeds with installing the package, showing UI if necessary. This is an "entry point" method that is only used
     * during first run. It populates the UI with the default initial data.
     *
     * @param pkg the ADB package data
     * @return a non-cancellable future with the result of the installation
     */
    private CompletableFuture<Result> installPackage(SdkPackage pkg) {
        return installPackageWithState(
                pkg,
                false,
                new File(configurationLocation.getLocalConfigurationDir(), "android-sdk")
        );
    }

    /**
     * Proceeds with installing the package, showing the UI if necessary. The UI state is based on the arguments.
     *
     * @param pkg the package to install
     * @param licenseAccepted whether the license is already accepted
     * @param initialInstallLocation the installation location to use
     * @return a non-cancellable future with the result of the installation
     */
    private CompletableFuture<Result> installPackageWithState(
            SdkPackage pkg,
            boolean licenseAccepted,
            File initialInstallLocation
    ) {
        return showAdbInstallDialog(pkg, licenseAccepted, initialInstallLocation)
                .thenComposeAsync(
                        installDir ->
                                startDownloadAndUnpack(pkg, SdkRepository.InstallMode.FAIL_IF_NOT_EMPTY, installDir),
                        uiExecutor
                );
    }

    private CompletableFuture<Result> startDownloadAndUnpack(
            SdkPackage pkg,
            SdkRepository.InstallMode installMode,
            File installDir) {
        return showInstallProgressDialog(
                downloadAndUnpack(
                        pkg,
                        installDir,
                        installMode)
        ).thenComposeAsync(
                maybeInstallDir ->
                        maybeInstallDir.<Result>map(sdkRoot -> Result.installed(getAdbExecutablePath(sdkRoot)))
                                .map(CompletableFuture::completedFuture)
                                .mapFailure(th -> onPackageDownloadError(pkg, installDir, th))
                                .get(),
                uiExecutor
        );
    }

    /**
     * Handles the failure of package downloading. Shows an error dialog and allows the user to select how to proceed.
     *
     * @param pkg the package that failed to download/unpack
     * @param installDir the directory that was selected for installation
     * @param failure the failure
     * @return the new result for the pipeline
     */
    private CompletableFuture<Result> onPackageDownloadError(SdkPackage pkg, File installDir, Throwable failure) {
        log.error("Failed to download package {}", pkg, failure);

        // Check if it's a DirectoryNotEmptyException
        if (failure instanceof TargetDirectoryNotEmptyException dirNotEmpty) {
            return onDirectoryNotEmpty(pkg, dirNotEmpty.getDirectory());
        }

        // Handle other errors with existing FailureView
        var failureResponse = new CompletableFuture<Result>();

        failureView.get().show(
                formatFailureMessage(failure),
                failure,
                () -> MyFutures.connect(installPackageWithState(pkg, true, installDir), failureResponse),
                () -> failureResponse.complete(Result.manual()),
                () -> failureResponse.cancel(false)
        );
        return failureResponse;
    }

    private static String formatFailureMessage(Throwable failure) {
        if (failure instanceof SdkException sdkException) {
            return sdkException.getMessage();
        }

        var message = failure.getMessage();
        if (message == null || message.isEmpty()) {
            return String.format(
                    "Unexpected exception %s when downloading the package.",
                    failure.getClass().getSimpleName()
            );
        }

        return String.format(
                "Unexpected exception %s when downloading the package: %s.",
                failure.getClass().getSimpleName(),
                message.endsWith(".") ? message.substring(0, message.length() - 1) : message
        );
    }

    /**
     * Handles the case when the user selects a non-empty directory for installation.
     * Shows a warning dialog and allows the user to continue anyway, choose another directory, or cancel.
     *
     * @param pkg the package being installed
     * @param installLocation the non-empty directory
     * @return the new result for the pipeline
     */
    private CompletableFuture<Result> onDirectoryNotEmpty(SdkPackage pkg, File installLocation) {
        var response = new CompletableFuture<Result>();
        directoryWarningView.get().show(
                installLocation,
                // Continue anyway - retry with OVERWRITE mode
                () -> MyFutures.connect(
                        startDownloadAndUnpack(pkg, SdkRepository.InstallMode.OVERWRITE, installLocation),
                        response
                ),
                // Choose another - go back to InstallView with license accepted
                () -> MyFutures.connect(
                        installPackageWithState(pkg, true, installLocation),
                        response
                ),
                // Cancel
                () -> response.cancel(false)
        );
        return response;
    }

    /**
     * Shows the installation dialog for the user to accept the license and select the installation location if desired.
     *
     * @param pkg the ADB package
     * @param isLicenseAccepted whether the license is already accepted
     * @param installLocation the initial install location to use
     * @return the future with the selected installation path, will be canceled if the user aborts install
     */
    private CompletableFuture<File> showAdbInstallDialog(
            SdkPackage pkg, boolean isLicenseAccepted, File installLocation) {
        var result = new CompletableFuture<File>();
        var view = installView.get();
        view.setLicenseText(pkg.getLicense());
        view.setInstallLocation(installLocation);
        view.setLicenseAccepted(isLicenseAccepted);
        view.setDownloadAllowed(isLicenseAccepted);
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
     *
     * @param pkg the package to download and unpack
     * @param installDir the target installation directory
     * @param mode the installation mode
     * @return the future with the resulting unpacked location of the package or with the download failure
     */
    private CompletableFuture<Try<File>> downloadAndUnpack(
            SdkPackage pkg,
            File installDir,
            SdkRepository.InstallMode mode
    ) {
        return MyFutures.runAsync(
                () -> Try.ofCallable(() -> {
                    repository.downloadPackage(pkg, installDir, mode);
                    return installDir;
                }),
                networkExecutor
        );
    }

    /**
     * Shows the progress dialog for ADB downloading and installing that can be cancelled by the user.
     *
     * @param install the installation future, will be cancelled if the user cancels the dialog
     * @return the decorated future that handles the dialog UI
     */
    private CompletableFuture<Try<File>> showInstallProgressDialog(CompletableFuture<Try<File>> install) {
        var view = downloadView.get();
        var completion = install.whenCompleteAsync((r, th) -> view.hide(), uiExecutor);
        view.show(MyFutures.toCancellable(install)::cancel);
        return completion;
    }

    @VisibleForTesting
    static File getAdbExecutablePath(File sdkRoot) {
        return sdkRoot.toPath().resolve(PLATFORM_TOOLS_PACKAGE).resolve(AdbLocationDiscovery.ADB_EXECUTABLE).toFile();
    }
}

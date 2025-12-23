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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.base.concurrent.TestExecutor;
import name.mlopatkin.andlogview.sdkrepo.SdkException;
import name.mlopatkin.andlogview.sdkrepo.SdkPackage;
import name.mlopatkin.andlogview.sdkrepo.SdkRepository;
import name.mlopatkin.andlogview.sdkrepo.TestSdkPackage;
import name.mlopatkin.andlogview.ui.preferences.InstallAdbPresenter.Cancelled;
import name.mlopatkin.andlogview.ui.preferences.InstallAdbPresenter.Installed;
import name.mlopatkin.andlogview.ui.preferences.InstallAdbPresenter.ManualFallback;
import name.mlopatkin.andlogview.ui.preferences.InstallAdbPresenter.PackageNotFound;
import name.mlopatkin.andlogview.ui.preferences.InstallAdbPresenter.Result;

import com.google.common.util.concurrent.MoreExecutors;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

abstract class DownloadAdbPresenterTestBase {
    protected final SdkRepository sdkRepository = mock();

    private @Nullable FakeProgressView initView;
    private @Nullable FakeProgressView downloadView;
    private @Nullable FakeInstallView installView;
    private @Nullable FakeFailureView failureView;
    private @Nullable FakeDirectoryWarningView directoryWarningView;

    private final TestExecutor netExecutor = new TestExecutor();

    @BeforeEach
    void setUp() throws IOException {
        withSdkPackageDownloadSucceeding();
    }

    protected void withSdkPackageNotFound() throws Exception {
        when(sdkRepository.locatePackage(DownloadAdbPresenter.PLATFORM_TOOLS_PACKAGE)).thenReturn(Optional.empty());
    }

    protected void withSdkPackageLocateFailing() throws Exception {
        withSdkPackageLocateFailingWith(new IOException("Failed to connect to repository"));
    }

    protected void withSdkPackageLocateFailingWith(Exception ex) throws Exception {
        when(sdkRepository.locatePackage(DownloadAdbPresenter.PLATFORM_TOOLS_PACKAGE))
                .thenThrow(ex);
    }

    protected CompletableFuture<Result> withSdkPackageLoaded(
            DownloadAdbPresenter presenter,
            SdkPackage aPackage
    ) throws Exception {
        when(sdkRepository.locatePackage(DownloadAdbPresenter.PLATFORM_TOOLS_PACKAGE))
                .thenReturn(Optional.of(aPackage));

        var r = presenter.startInstall();

        completePendingActions();

        return r;
    }

    protected CompletableFuture<Result> withSdkPackageLoadedAndLicenseAccepted(
            DownloadAdbPresenter presenter,
            SdkPackage aPackage
    ) throws Exception {
        var r = withSdkPackageLoaded(presenter, aPackage);
        installView().runAcceptStateChange(true);
        return r;
    }

    protected CompletableFuture<Result> withSdkPackageInstallReady(
            DownloadAdbPresenter presenter,
            SdkPackage aPackage,
            File installPath
    ) throws Exception {
        var r = withSdkPackageLoadedAndLicenseAccepted(presenter, aPackage);

        installView().setInstallLocation(installPath);
        installView().runCommitAction(installPath);

        return r;
    }

    protected void completePendingActions() {
        netExecutor.flush();
    }

    protected DownloadAdbPresenter createPresenter() {
        return new DownloadAdbPresenter(
                sdkRepository,
                this::newInitView,
                this::newInstallView,
                this::newFailureView,
                this::newDirectoryWarningView,
                this::newDownloadView,
                MoreExecutors.directExecutor(),
                netExecutor
        );
    }

    protected FakeProgressView newInitView() {
        assertTrue(initView == null || !initView.isShown(), "initView must be disposed");
        return initView = new FakeProgressView();
    }

    protected FakeInstallView newInstallView() {
        assertTrue(installView == null || !installView.isShown(), "installView must be disposed");
        return installView = new FakeInstallView();
    }

    protected FakeFailureView newFailureView() {
        assertTrue(failureView == null || !failureView().isShown(), "failureView must be disposed");
        return failureView = new FakeFailureView();
    }

    protected FakeProgressView newDownloadView() {
        assertTrue(downloadView == null || !downloadView().isShown(), "downloadView must be disposed");
        return downloadView = new FakeProgressView();
    }

    protected FakeDirectoryWarningView newDirectoryWarningView() {
        assertTrue(directoryWarningView == null || !directoryWarningView().isShown(),
                   "directoryWarningView must be disposed");
        return directoryWarningView = new FakeDirectoryWarningView();
    }


    protected FakeProgressView initView() {
        return Objects.requireNonNull(initView, "initView not created");
    }

    protected FakeInstallView installView() {
        return Objects.requireNonNull(installView, "installView not created");
    }

    protected FakeFailureView failureView() {
        return Objects.requireNonNull(failureView, "failureView not created");
    }

    protected FakeProgressView downloadView() {
        return Objects.requireNonNull(downloadView, "downloadView not created");
    }

    protected FakeDirectoryWarningView directoryWarningView() {
        return Objects.requireNonNull(directoryWarningView, "directoryWarningView not created");
    }

    protected void assertViewsHidden() {
        assertTrue(initView == null || !initView().isShown(), "initView must be disposed");
        assertTrue(installView == null || !installView().isShown(), "installView must be disposed");
        assertTrue(failureView == null || !failureView().isShown(), "failureView must be disposed");
        assertTrue(downloadView == null || !downloadView().isShown(), "downloadView must be disposed");
        assertTrue(directoryWarningView == null || !directoryWarningView().isShown(),
                   "directoryWarningView must be disposed");
    }

    protected void assertOnlyShows(View view) {
        assertTrue(view == initView || view == installView || view == failureView || view == downloadView
                   || view == directoryWarningView);
        assertViewShown(initView, initView == view);
        assertViewShown(installView, installView == view);
        assertViewShown(failureView, failureView == view);
        assertViewShown(downloadView, downloadView == view);
        assertViewShown(directoryWarningView, directoryWarningView == view);
    }

    protected void assertViewShown(@Nullable View view, boolean expectedState) {
        if (expectedState) {
            assertNotNull(view);
            assertTrue(view.isShown());
        } else {
            assertTrue(view == null || !view.isShown());
        }
    }

    protected void assertFinishedSuccessfully(CompletableFuture<Result> result, File expectedSdkRoot) {
        assertViewsHidden();
        File expectedAdbPath = DownloadAdbPresenter.getAdbExecutablePath(expectedSdkRoot);
        assertThat(result).isCompletedWithValueMatching(
                r -> r instanceof Installed installed && expectedAdbPath.equals(installed.getAdbPath()),
                "is installed with adb at " + expectedAdbPath
        );
    }

    protected void assertFinishedCancelled(CompletableFuture<Result> result) {
        assertViewsHidden();
        assertThat(result).isCompletedWithValueMatching(Cancelled.class::isInstance);
    }

    protected void assertFinishedWithPackageNotFound(CompletableFuture<Result> result) {
        assertViewsHidden();
        assertThat(result).isCompletedWithValueMatching(PackageNotFound.class::isInstance);
    }

    protected void assertFinishedWithManualFallback(CompletableFuture<Result> result) {
        assertViewsHidden();
        assertThat(result).isCompletedWithValueMatching(ManualFallback.class::isInstance);
    }

    protected void assertFinishedWithFailure(CompletableFuture<Result> result) {
        assertViewsHidden();
        assertThat(result).isCompletedExceptionally();
    }

    protected SdkPackage createPackage() {
        return TestSdkPackage.createPackage(DownloadAdbPresenter.PLATFORM_TOOLS_PACKAGE);
    }

    protected void withSdkPackageDownloadFailing() throws IOException {
        doThrow(new SdkException("Failure")).when(sdkRepository).downloadPackage(any(), any(), any());
    }

    protected void withSdkPackageDownloadSucceeding() throws IOException {
        doAnswer(invocation -> null).when(sdkRepository).downloadPackage(any(), any(), any());
    }

    protected static class FakeProgressView
            implements DownloadAdbPresenter.SdkInitView, DownloadAdbPresenter.SdkDownloadView, View {
        private boolean isShown = false;
        private boolean wasEverShown = false;
        private @Nullable Runnable cancelAction = null;

        @Override
        public void show(Runnable cancelAction) {
            assertFalse(wasEverShown, "Dialog cannot be shown more than once");
            this.isShown = true;
            this.wasEverShown = true;
            this.cancelAction = cancelAction;
        }

        @Override
        public void hide() {
            this.isShown = false;
            this.cancelAction = null;
        }

        @Override
        public boolean isShown() {
            return isShown;
        }

        public void runCancelAction() {
            assertNotNull(cancelAction, "Cannot run cancel action before the dialog is shown");
            assertTrue(isShown, "Cannot cancel if dialog is hidden");
            cancelAction.run();
        }
    }

    protected static class FakeInstallView implements DownloadAdbPresenter.InstallView, View {
        private @Nullable Consumer<Boolean> acceptAction;
        private @Nullable Runnable installLocationSelectionAction;
        private @Nullable Consumer<? super File> showCommitAction;
        private @Nullable Runnable showCancelAction;
        private @Nullable Consumer<? super File> selectorCommitAction;
        private @Nullable Runnable selectorCancelAction;

        private @Nullable String licenseText;
        private @Nullable File installLocation;
        private boolean downloadAllowed;
        private boolean isShown;
        private boolean isFileSelectorShown;
        private boolean licenseAccepted;

        @Override
        public void setAcceptAction(Consumer<Boolean> acceptAction) {
            this.acceptAction = Objects.requireNonNull(acceptAction);
        }

        @Override
        public void setLicenseAccepted(boolean licenseAccepted) {
            this.licenseAccepted = licenseAccepted;
        }

        @Override
        public void setLicenseText(String licenseText) {
            this.licenseText = Objects.requireNonNull(licenseText);
        }

        @Override
        public void setInstallLocation(File path) {
            this.installLocation = Objects.requireNonNull(path);
        }

        @Override
        public void setInstallLocationSelectionAction(Runnable action) {
            this.installLocationSelectionAction = Objects.requireNonNull(action);
        }

        @Override
        public void showInstallDirSelector(Consumer<? super File> commitAction, Runnable cancelAction) {
            assertTrue(isShown, "Cannot show file selector if the dialog is hidden");
            assertFalse(isFileSelectorShown, "File selector is already shown");

            this.selectorCommitAction = commitAction;
            this.selectorCancelAction = cancelAction;
            this.isFileSelectorShown = true;
        }

        @Override
        public void show(Consumer<? super File> commitAction, Runnable cancelAction) {
            assertNotNull(licenseText, "License not set before showing the dialog");
            this.showCommitAction = commitAction;
            this.showCancelAction = cancelAction;
            this.isShown = true;
        }

        @Override
        public void hide() {
            this.isShown = false;
        }

        @Override
        public void setDownloadAllowed(boolean allowed) {
            this.downloadAllowed = allowed;
        }

        public void runAcceptStateChange(boolean accepted) {
            assertNotNull(acceptAction, "Accept action not set");
            licenseAccepted = accepted;
            acceptAction.accept(accepted);
        }

        public void runLocationSelectionAction() {
            assertNotNull(installLocationSelectionAction, "Install location selection action not set");
            installLocationSelectionAction.run();
        }

        public void runCommitAction(File selectedDir) {
            assertNotNull(showCommitAction, "Show commit action not set");
            showCommitAction.accept(selectedDir);
        }

        public void runCancelAction() {
            assertNotNull(showCancelAction, "Show cancel action not set");
            assertTrue(isShown, "Cannot cancel if dialog is hidden");
            showCancelAction.run();
        }

        public void runSelectFileAction(File selectedDir) {
            assertTrue(isFileSelectorShown, "File selector not shown");
            assertNotNull(selectorCommitAction, "Selector commit action not set");
            isFileSelectorShown = false;
            selectorCommitAction.accept(selectedDir);
        }

        public void runCancelFileSelectorAction() {
            assertTrue(isFileSelectorShown, "File selector not shown");
            assertNotNull(selectorCancelAction, "Selector cancel action not set");
            isFileSelectorShown = false;
            selectorCancelAction.run();
        }

        @Override
        public boolean isShown() {
            return isShown;
        }

        public boolean isDownloadAllowed() {
            return downloadAllowed;
        }

        public @Nullable String getLicenseText() {
            return licenseText;
        }

        public @Nullable File getInstallLocation() {
            return installLocation;
        }

        public boolean isFileSelectorShown() {
            return isFileSelectorShown;
        }

        public boolean isLicenseAccepted() {
            return licenseAccepted;
        }
    }

    protected static class FakeFailureView implements DownloadAdbPresenter.FailureView, View {
        private @Nullable String message;
        private @Nullable Runnable tryAgainAction;
        private @Nullable Runnable installManuallyAction;
        private @Nullable Runnable cancelAction;
        private boolean isShown;

        @Override
        public void show(
                String message,
                Throwable failure,
                Runnable tryAgain,
                Runnable installManually,
                Runnable cancel
        ) {
            this.message = Objects.requireNonNull(message);
            this.tryAgainAction = Objects.requireNonNull(tryAgain);
            this.installManuallyAction = Objects.requireNonNull(installManually);
            this.cancelAction = Objects.requireNonNull(cancel);
            this.isShown = true;
        }

        public void hide() {
            this.isShown = false;
        }

        public void runTryAgain() {
            assertTrue(isShown, "Dialog not shown");
            assertNotNull(tryAgainAction, "Try again action not set");
            hide();
            tryAgainAction.run();
        }

        public void runInstallManually() {
            assertTrue(isShown, "Dialog not shown");
            assertNotNull(installManuallyAction, "Install manually action not set");
            hide();
            installManuallyAction.run();
        }

        public void runCancel() {
            assertTrue(isShown, "Dialog not shown");
            assertNotNull(cancelAction, "Cancel action not set");
            hide();
            cancelAction.run();
        }

        @Override
        public boolean isShown() {
            return isShown;
        }

        public @Nullable String getMessage() {
            return message;
        }
    }

    protected static class FakeDirectoryWarningView implements DownloadAdbPresenter.DirectoryWarningView, View {
        private @Nullable File directory;
        private @Nullable Runnable continueAnywayAction;
        private @Nullable Runnable chooseAnotherAction;
        private @Nullable Runnable cancelAction;
        private boolean isShown;

        @Override
        public void show(File directory, Runnable continueAnyway, Runnable chooseAnother, Runnable cancel) {
            this.directory = Objects.requireNonNull(directory);
            this.continueAnywayAction = Objects.requireNonNull(continueAnyway);
            this.chooseAnotherAction = Objects.requireNonNull(chooseAnother);
            this.cancelAction = Objects.requireNonNull(cancel);
            this.isShown = true;
        }

        public void hide() {
            this.isShown = false;
        }

        public void runContinueAnyway() {
            assertTrue(isShown, "Dialog not shown");
            assertNotNull(continueAnywayAction);
            hide();
            continueAnywayAction.run();
        }

        public void runChooseAnother() {
            assertTrue(isShown, "Dialog not shown");
            assertNotNull(chooseAnotherAction);
            hide();
            chooseAnotherAction.run();
        }

        public void runCancel() {
            assertTrue(isShown, "Dialog not shown");
            assertNotNull(cancelAction);
            hide();
            cancelAction.run();
        }

        @Override
        public boolean isShown() {
            return isShown;
        }

        public @Nullable File getDirectory() {
            return directory;
        }
    }

    protected interface View {
        boolean isShown();
    }
}

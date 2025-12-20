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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.base.concurrent.TestExecutor;
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
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings("FutureReturnValueIgnored")
class DownloadAdbPresenterTest {

    private final SdkRepository sdkRepository = mock();

    private @Nullable FakeProgressView initView;
    private @Nullable FakeProgressView downloadView;
    private @Nullable FakeInstallView installView;
    private @Nullable FakeFailureView failureView;

    private final TestExecutor uiExecutor = new TestExecutor();
    private final TestExecutor netExecutor = new TestExecutor();

    @BeforeEach
    void setUp() throws IOException {
        withSdkPackageDownloadSucceeding();
    }

    @Test
    void showsProgressViewToInitSdk() throws Exception {
        var presenter = createPresenter();

        var result = presenter.startInstall();

        assertThat(result).isNotDone();
        assertThat(initView().isShown()).isTrue();
    }

    @Test
    void canCancelSdkInit() {
        var presenter = createPresenter();

        var result = presenter.startInstall();

        initView().runCancelAction();
        completePendingActions();

        assertFinishedCancelled(result);
    }

    private void withSdkPackageNotFound() throws Exception {
        when(sdkRepository.locatePackage(DownloadAdbPresenter.PLATFORM_TOOLS_PACKAGE)).thenReturn(Optional.empty());
    }

    @Test
    void fallsBackWhenSdkPackageIsNotAvailable() throws Exception {
        withSdkPackageNotFound();

        var presenter = createPresenter();
        var result = presenter.startInstall();

        completePendingActions();

        assertFinishedWithPackageNotFound(result);
    }

    private CompletableFuture<Result> withSdkPackageLoaded(
            DownloadAdbPresenter presenter,
            SdkPackage aPackage
    ) throws Exception {
        when(sdkRepository.locatePackage(DownloadAdbPresenter.PLATFORM_TOOLS_PACKAGE))
                .thenReturn(Optional.of(aPackage));

        var r = presenter.startInstall();

        completePendingActions();

        return r;
    }

    @Test
    void showsDialogButDoesNotProceedWithDownloadUntilLicenseAccepted() throws Exception {
        var presenter = createPresenter();
        var result = withSdkPackageLoaded(presenter, createPackage());

        assertOnlyShows(installView());
        assertThat(installView().getLicenseText()).isEqualTo(TestSdkPackage.TEST_LICENSE);
        assertThat(installView().isDownloadAllowed()).isFalse();

        assertThat(result).isNotCompleted();
        verify(sdkRepository, never()).downloadPackage(any(), any());
    }

    @Test
    void canCancelWhenPresentedLicense() throws Exception {
        var presenter = createPresenter();
        var result = withSdkPackageLoaded(presenter, createPackage());

        installView().runCancelAction();

        assertFinishedCancelled(result);
        verify(sdkRepository, never()).downloadPackage(any(), any());
    }

    @Test
    void acceptingLicenseAllowsToProceed() throws Exception {
        var presenter = createPresenter();
        withSdkPackageLoaded(presenter, createPackage());

        installView().runAcceptStateChange(true);

        assertOnlyShows(installView());
        assertThat(installView().isDownloadAllowed()).isTrue();
    }

    @Test
    void rejectingLicenseDoesNotAllowToProceed() throws Exception {
        var presenter = createPresenter();
        withSdkPackageLoaded(presenter, createPackage());

        installView().runAcceptStateChange(true);
        installView().runAcceptStateChange(false);

        assertOnlyShows(installView());
        assertThat(installView().isDownloadAllowed()).isFalse();
    }

    @Test
    void providesDefaultInstallLocation() throws Exception {
        var presenter = createPresenter();
        withSdkPackageLoaded(presenter, createPackage());

        assertOnlyShows(installView());
        assertThat(installView().getInstallLocation()).isNotNull();
    }

    private CompletableFuture<Result> withSdkPackageLoadedAndLicenseAccepted(
            DownloadAdbPresenter presenter,
            SdkPackage aPackage
    ) throws Exception {
        var r = withSdkPackageLoaded(presenter, aPackage);
        installView().runAcceptStateChange(true);
        return r;
    }

    @Test
    void canSelectInstallLocation() throws Exception {
        var presenter = createPresenter();
        withSdkPackageLoadedAndLicenseAccepted(presenter, createPackage());

        installView().runLocationSelectionAction();

        var newFile = new File("test/changed");
        installView().runSelectFileAction(newFile);

        assertOnlyShows(installView());
        assertThat(installView().isFileSelectorShown()).isFalse();
        assertThat(installView().getInstallLocation()).isEqualTo(newFile);
    }

    @Test
    void canCancelSelectingInstallLocation() throws Exception {
        var presenter = createPresenter();
        withSdkPackageLoadedAndLicenseAccepted(presenter, createPackage());
        var currentFile = new File("test/current");

        installView().setInstallLocation(currentFile);

        installView().runLocationSelectionAction();
        installView().runCancelFileSelectorAction();

        assertOnlyShows(installView());
        assertThat(installView().isFileSelectorShown()).isFalse();
        assertThat(installView().getInstallLocation()).isEqualTo(currentFile);
    }

    private CompletableFuture<Result> withSdkPackageInstallReady(
            DownloadAdbPresenter presenter,
            SdkPackage aPackage,
            File installPath
    ) throws Exception {
        var r = withSdkPackageLoadedAndLicenseAccepted(presenter, aPackage);

        installView().setInstallLocation(installPath);
        installView().runCommitAction(installPath);

        return r;
    }

    @Test
    void downloadsSdkToAppropriateDirectory() throws Exception {
        var presenter = createPresenter();
        var installDir = new File("installDir");
        var result = withSdkPackageInstallReady(presenter, createPackage(), installDir);

        assertThat(installView().isShown()).isFalse(); // progressing hides the license dialog
        assertThat(downloadView().isShown()).isTrue(); // downloadView appears immediately

        completePendingActions();

        assertFinishedSuccessfully(result, installDir);
    }

    @Test
    void showsErrorDialogWhenDownloadingPackageFails() throws Exception {
        var aPackage = createPackage();
        var installDir = new File("installDir");
        withSdkPackageDownloadFailing();

        var presenter = createPresenter();
        var result = withSdkPackageInstallReady(presenter, aPackage, installDir);

        completePendingActions();

        assertOnlyShows(failureView());
        assertThat(result).isNotCompleted();
    }

    @Test
    void userCanRetryWhenDownloadingPackageFails() throws Exception {
        var aPackage = createPackage();
        var installDir = new File("installDir");
        withSdkPackageDownloadFailing();

        var presenter = createPresenter();
        var result = withSdkPackageInstallReady(presenter, aPackage, installDir);

        completePendingActions();

        failureView().runTryAgain();
        completePendingActions();

        assertOnlyShows(installView());
        assertThat(installView().isLicenseAccepted()).isTrue();
        assertThat(result).isNotCompleted();
    }

    @Test
    void retryAfterDownloadingFailsCanSucceed() throws Exception {
        var aPackage = createPackage();
        var installDir = new File("installDir");
        withSdkPackageDownloadFailing();

        var presenter = createPresenter();
        var result = withSdkPackageInstallReady(presenter, aPackage, installDir);

        completePendingActions();

        withSdkPackageDownloadSucceeding();
        failureView().runTryAgain();

        installView().runCommitAction(installDir);
        completePendingActions();

        assertFinishedSuccessfully(result, installDir);
    }

    @Test
    void userCanCancelWhenDownloadingPackageFails() throws Exception {
        var aPackage = createPackage();
        var installDir = new File("installDir");
        withSdkPackageDownloadFailing();

        var presenter = createPresenter();
        var result = withSdkPackageInstallReady(presenter, aPackage, installDir);

        completePendingActions();

        failureView().runCancel();
        completePendingActions();

        assertFinishedCancelled(result);
    }

    @Test
    void userCanFallBackToManualDownloadWhenDownloadingPackageFails() throws Exception {
        var aPackage = createPackage();
        var installDir = new File("installDir");
        withSdkPackageDownloadFailing();

        var presenter = createPresenter();
        var result = withSdkPackageInstallReady(presenter, aPackage, installDir);

        completePendingActions();

        failureView().runInstallManually();
        completePendingActions();

        assertFinishedWithManualFallback(result);
    }

    @Test
    void userCanCancelAfterTryingAgain() throws Exception {
        withSdkPackageDownloadFailing();

        var aPackage = createPackage();
        var installDir = new File("installDir");

        var presenter = createPresenter();
        var result = withSdkPackageInstallReady(presenter, aPackage, installDir);

        completePendingActions();
        failureView().runTryAgain();
        completePendingActions();
        installView().runCancelAction();
        completePendingActions();

        assertFinishedCancelled(result);
    }

    @Test
    void canCancelRunningDownload() throws Exception {
        var presenter = createPresenter();
        var installDir = new File("installDir");
        var result = withSdkPackageInstallReady(presenter, createPackage(), installDir);

        downloadView().runCancelAction();

        assertFinishedCancelled(result);
    }

    private void completePendingActions() {
        do {
            netExecutor.flush();
        } while (uiExecutor.flush());
    }

    private DownloadAdbPresenter createPresenter() {
        return new DownloadAdbPresenter(
                sdkRepository,
                this::newInitView,
                this::newInstallView,
                this::newFailureView,
                this::newDownloadView,
                MoreExecutors.directExecutor(),
                netExecutor
        );
    }

    private FakeProgressView newInitView() {
        assertTrue(initView == null || !initView.isShown(), "initView must be disposed");
        return initView = new FakeProgressView();
    }

    private FakeInstallView newInstallView() {
        assertTrue(installView == null || !installView.isShown(), "installView must be disposed");
        return installView = new FakeInstallView();
    }

    private FakeFailureView newFailureView() {
        assertTrue(failureView == null || !failureView().isShown(), "failureView must be disposed");
        return failureView = new FakeFailureView();
    }

    private FakeProgressView newDownloadView() {
        assertTrue(downloadView == null || !downloadView().isShown(), "downloadView must be disposed");
        return downloadView = new FakeProgressView();
    }


    private FakeProgressView initView() {
        return Objects.requireNonNull(initView, "initView not created");
    }

    private FakeInstallView installView() {
        return Objects.requireNonNull(installView, "installView not created");
    }

    private FakeFailureView failureView() {
        return Objects.requireNonNull(failureView, "failureView not created");
    }

    private FakeProgressView downloadView() {
        return Objects.requireNonNull(downloadView, "downloadView not created");
    }

    private void assertViewsHidden() {
        assertTrue(initView == null || !initView().isShown(), "initView must be disposed");
        assertTrue(installView == null || !installView().isShown(), "installView must be disposed");
        assertTrue(failureView == null || !failureView().isShown(), "failureView must be disposed");
        assertTrue(downloadView == null || !downloadView().isShown(), "downloadView must be disposed");
    }

    private void assertOnlyShows(View view) {
        assertTrue(view == initView || view == installView || view == failureView || view == downloadView);
        assertViewShown(initView, initView == view);
        assertViewShown(installView, installView == view);
        assertViewShown(failureView, failureView == view);
        assertViewShown(downloadView, downloadView == view);
    }

    private void assertViewShown(@Nullable View view, boolean expectedState) {
        if (expectedState) {
            assertNotNull(view);
            assertTrue(view.isShown());
        } else {
            assertTrue(view == null || !view.isShown());
        }
    }

    private void assertFinishedSuccessfully(CompletableFuture<Result> result, File expectedPath) {
        assertViewsHidden();
        assertThat(result).isCompletedWithValueMatching(
                r -> r instanceof Installed installed && expectedPath.equals(installed.getAdbPath()),
                "is installed in " + expectedPath
        );
    }

    private void assertFinishedCancelled(CompletableFuture<Result> result) {
        assertViewsHidden();
        assertThat(result).isCompletedWithValueMatching(Cancelled.class::isInstance);
    }

    private void assertFinishedWithPackageNotFound(CompletableFuture<Result> result) {
        assertViewsHidden();
        assertThat(result).isCompletedWithValueMatching(PackageNotFound.class::isInstance);
    }

    private void assertFinishedWithManualFallback(CompletableFuture<Result> result) {
        assertThat(result).isCompletedWithValueMatching(ManualFallback.class::isInstance);
        assertViewsHidden();
    }

    private SdkPackage createPackage() {
        return TestSdkPackage.createPackage(DownloadAdbPresenter.PLATFORM_TOOLS_PACKAGE);
    }

    private void withSdkPackageDownloadFailing() throws IOException {
        doThrow(IOException.class).when(sdkRepository).downloadPackage(any(), any());
    }

    private void withSdkPackageDownloadSucceeding() throws IOException {
        doAnswer(invocation -> null).when(sdkRepository).downloadPackage(any(), any());
    }

    static class FakeProgressView
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

    static class FakeInstallView implements DownloadAdbPresenter.InstallView, View {
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

    static class FakeFailureView implements DownloadAdbPresenter.FailureView, View {
        private @Nullable String message;
        private @Nullable Runnable tryAgainAction;
        private @Nullable Runnable installManuallyAction;
        private @Nullable Runnable cancelAction;
        private boolean isShown;

        @Override
        public void show(String message, Runnable tryAgain, Runnable installManually, Runnable cancel) {
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

    interface View {
        boolean isShown();
    }
}

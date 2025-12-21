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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import name.mlopatkin.andlogview.sdkrepo.TestSdkPackage;
import name.mlopatkin.andlogview.ui.preferences.InstallAdbPresenter.Result;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * Tests license and target directory configuration dialog.
 */
class DownloadAdbPresenterLicenseAndTargetTest extends DownloadAdbPresenterTestBase {

    @Test
    void showsDialogButDoesNotProceedWithDownloadUntilLicenseAccepted() throws Exception {
        var presenter = createPresenter();
        var result = withSdkPackageLoaded(presenter, createPackage());

        assertOnlyShows(installView());
        assertThat(installView().getLicenseText()).isEqualTo(TestSdkPackage.TEST_LICENSE);
        assertThat(installView().isDownloadAllowed()).isFalse();

        assertDownloadNotStarted(result);
    }

    @Test
    void canCancelWhenPresentedLicense() throws Exception {
        var presenter = createPresenter();
        var result = withSdkPackageLoaded(presenter, createPackage());

        installView().runCancelAction();

        assertFinishedCancelled(result);
    }

    @Test
    void acceptingLicenseAllowsToProceed() throws Exception {
        var presenter = createPresenter();
        var result = withSdkPackageLoaded(presenter, createPackage());

        installView().runAcceptStateChange(true);

        assertOnlyShows(installView());
        assertThat(installView().isDownloadAllowed()).isTrue();

        assertDownloadNotStarted(result);
    }

    @Test
    void rejectingLicenseDoesNotAllowToProceed() throws Exception {
        var presenter = createPresenter();
        var result = withSdkPackageLoaded(presenter, createPackage());

        installView().runAcceptStateChange(true);
        installView().runAcceptStateChange(false);

        assertOnlyShows(installView());
        assertThat(installView().isDownloadAllowed()).isFalse();

        assertDownloadNotStarted(result);
    }

    @Test
    void providesDefaultInstallLocation() throws Exception {
        var presenter = createPresenter();
        var result = withSdkPackageLoaded(presenter, createPackage());

        assertOnlyShows(installView());
        assertThat(installView().getInstallLocation()).isNotNull();

        assertDownloadNotStarted(result);
    }

    @Test
    void canSelectInstallLocation() throws Exception {
        var presenter = createPresenter();

        var result = withSdkPackageLoadedAndLicenseAccepted(presenter, createPackage());

        installView().runLocationSelectionAction();
        var newFile = new File("test/changed");
        installView().runSelectFileAction(newFile);

        assertOnlyShows(installView());
        assertThat(installView().isFileSelectorShown()).isFalse();
        assertThat(installView().getInstallLocation()).isEqualTo(newFile);

        assertDownloadNotStarted(result);
    }

    @Test
    void canCancelSelectingInstallLocation() throws Exception {
        var presenter = createPresenter();
        var result = withSdkPackageLoadedAndLicenseAccepted(presenter, createPackage());
        var currentFile = new File("test/current");

        installView().setInstallLocation(currentFile);

        installView().runLocationSelectionAction();
        installView().runCancelFileSelectorAction();

        assertOnlyShows(installView());
        assertThat(installView().isFileSelectorShown()).isFalse();
        assertThat(installView().getInstallLocation()).isEqualTo(currentFile);
        assertDownloadNotStarted(result);
    }

    private void assertDownloadNotStarted(CompletableFuture<Result> result) throws Exception {
        completePendingActions();
        assertThat(result).isNotCompleted();
        verify(sdkRepository, never()).downloadPackage(any(), any(), any());
    }
}

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

import org.junit.jupiter.api.Test;

import java.io.File;

class DownloadAdbPresenterPackageDownloadTest extends DownloadAdbPresenterTestBase {

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
}

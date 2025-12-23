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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

import name.mlopatkin.andlogview.sdkrepo.SdkException;
import name.mlopatkin.andlogview.sdkrepo.SdkRepository;
import name.mlopatkin.andlogview.sdkrepo.TargetDirectoryNotEmptyException;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class DownloadAdbPresenterDirectoryWarningTest extends DownloadAdbPresenterTestBase {
    @Test
    void showsWarningWhenDirectoryIsNotEmpty() throws Exception {
        var nonEmptyDir = new File("nonEmpty");
        withTargetDirectoryNotEmpty(nonEmptyDir);

        var presenter = createPresenter();
        var result = withSdkPackageInstallReady(presenter, createPackage(), nonEmptyDir);

        completePendingActions();

        assertOnlyShows(directoryWarningView());
        assertThat(directoryWarningView().getDirectory()).isEqualTo(nonEmptyDir);
        assertThat(result).isNotCompleted();
    }

    @Test
    void continueAnywayRetriesInOverwriteMode() throws Exception {
        var nonEmptyDir = new File("nonEmpty");
        withTargetDirectoryNotEmpty(nonEmptyDir);

        var presenter = createPresenter();
        var result = withSdkPackageInstallReady(presenter, createPackage(), nonEmptyDir);
        completePendingActions();

        directoryWarningView().runContinueAnyway();
        completePendingActions();

        assertFinishedSuccessfully(result, nonEmptyDir);
    }

    @Test
    void continueAnywayThenEncountersOtherErrorShowsFailureView() throws Exception {
        var nonEmptyDir = new File("nonEmpty");

        withTargetDirectoryNotEmpty(nonEmptyDir);

        withPackageOverwriteFailing(nonEmptyDir);

        var presenter = createPresenter();
        var result = withSdkPackageInstallReady(presenter, createPackage(), nonEmptyDir);
        completePendingActions();

        directoryWarningView().runContinueAnyway();
        completePendingActions();

        assertOnlyShows(failureView());
        assertThat(failureView().getMessage()).contains("Disk full");
        assertThat(result).isNotCompleted();
    }

    @Test
    void chooseAnotherReopensInstallDialogWithLicenseAccepted() throws Exception {
        var nonEmptyDir = new File("nonEmpty");
        withTargetDirectoryNotEmpty(nonEmptyDir);

        var presenter = createPresenter();
        var result = withSdkPackageInstallReady(presenter, createPackage(), nonEmptyDir);
        completePendingActions();

        directoryWarningView().runChooseAnother();

        assertOnlyShows(installView());
        assertThat(installView().isLicenseAccepted()).isTrue();
        assertThat(installView().isDownloadAllowed()).isTrue();
        assertThat(result).isNotCompleted();
    }

    @Test
    void chooseAnotherThenSelectNewDirectoryAndSucceed() throws Exception {
        var nonEmptyDir = new File("nonEmpty");
        var emptyDir = new File("empty");

        withTargetDirectoryNotEmpty(nonEmptyDir);

        var presenter = createPresenter();
        var result = withSdkPackageInstallReady(presenter, createPackage(), nonEmptyDir);
        completePendingActions();

        directoryWarningView().runChooseAnother();

        installView().setInstallLocation(emptyDir);
        installView().runCommitAction(emptyDir);
        completePendingActions();

        assertFinishedSuccessfully(result, emptyDir);
    }

    @Test
    void chooseAnotherThenEncounterDirectoryNotEmptyAgain() throws Exception {
        var firstNonEmptyDir = new File("nonEmpty1");
        var secondNonEmptyDir = new File("nonEmpty2");

        withTargetDirectoryNotEmpty(firstNonEmptyDir);
        withTargetDirectoryNotEmpty(secondNonEmptyDir);

        var presenter = createPresenter();
        var result = withSdkPackageInstallReady(presenter, createPackage(), firstNonEmptyDir);
        completePendingActions();

        directoryWarningView().runChooseAnother();
        installView().setInstallLocation(secondNonEmptyDir);
        installView().runCommitAction(secondNonEmptyDir);
        completePendingActions();

        assertOnlyShows(directoryWarningView());
        assertThat(directoryWarningView().getDirectory()).isEqualTo(secondNonEmptyDir);
        assertThat(result).isNotCompleted();
    }

    @Test
    void cancelFromWarningAbortsInstallation() throws Exception {
        var nonEmptyDir = new File("nonEmpty");
        withTargetDirectoryNotEmpty(nonEmptyDir);

        var presenter = createPresenter();
        var result = withSdkPackageInstallReady(presenter, createPackage(), nonEmptyDir);
        completePendingActions();

        directoryWarningView().runCancel();

        assertFinishedCancelled(result);
    }

    private void withTargetDirectoryNotEmpty(File nonEmptyDir) throws IOException {
        doThrow(new TargetDirectoryNotEmptyException(nonEmptyDir))
                .when(sdkRepository)
                .downloadPackage(any(), eq(nonEmptyDir), eq(SdkRepository.InstallMode.FAIL_IF_NOT_EMPTY));
    }

    private void withPackageOverwriteFailing(File nonEmptyDir) throws IOException {
        doThrow(new SdkException("Disk full"))
                .when(sdkRepository)
                .downloadPackage(any(), eq(nonEmptyDir), eq(SdkRepository.InstallMode.OVERWRITE));
    }
}

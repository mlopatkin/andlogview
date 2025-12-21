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

class DownloadAdbPresenterSdkInitTest extends DownloadAdbPresenterTestBase {

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

    @Test
    void fallsBackWhenSdkPackageIsNotAvailable() throws Exception {
        withSdkPackageNotFound();

        var presenter = createPresenter();
        var result = presenter.startInstall();

        completePendingActions();

        assertFinishedWithPackageNotFound(result);
    }

    @Test
    void failsWhenSdkInitializationThrowsException() throws Exception {
        withSdkPackageLocateFailing();

        var presenter = createPresenter();
        var result = presenter.startInstall();

        completePendingActions();

        assertFinishedWithFailure(result);
    }
}

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
import static org.mockito.Mockito.mock;

import name.mlopatkin.andlogview.base.concurrent.TestExecutor;
import name.mlopatkin.andlogview.sdkrepo.SdkRepository;

import com.google.common.util.concurrent.MoreExecutors;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

class DownloadAdbPresenterTest {
    private final SdkRepository sdkRepository = mock();
    private final FakeInitView initView = new FakeInitView();
    private final TestExecutor uiExecutor = new TestExecutor();
    private final TestExecutor netExecutor = new TestExecutor();

    @Test
    void showsProgressViewToInitSdk() {
        var presenter =
                createPresenter();

        var result = presenter.startInstall();

        assertThat(result).isNotDone();
        assertThat(initView.isShown()).isTrue();

        netExecutor.flush();

        assertThat(result).isCompleted();
        assertThat(initView.isShown()).isFalse();
    }

    @Test
    void canCancelSdkInit() {
        var presenter =
                createPresenter();

        var result = presenter.startInstall();

        initView.runCancelAction();

        netExecutor.flush();

        // TODO(mlopatkin) this isn't a precise description.
        assertThat(result).isDone();
        assertThat(initView.isShown()).isFalse();
    }

    private DownloadAdbPresenter createPresenter() {
        return new DownloadAdbPresenter(sdkRepository, () -> initView,
                () -> mock(DownloadAdbPresenter.InstallView.class), MoreExecutors.directExecutor(),
                netExecutor);
    }

    static class FakeInitView implements DownloadAdbPresenter.SdkInitView {
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

        public boolean isShown() {
            return isShown;
        }

        public void runCancelAction() {
            assertNotNull(cancelAction, "Cannot run cancel action before the dialog is shown");
            cancelAction.run();
        }
    }
}

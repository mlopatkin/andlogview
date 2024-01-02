/*
 * Copyright 2023 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.mainframe.device;

import name.mlopatkin.andlogview.ui.device.AdbServicesInitializationPresenter;
import name.mlopatkin.andlogview.ui.mainframe.ErrorDialogs;

import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

/**
 * A view to show ADB initialization in the Main Frame.
 */
class MainFrameAdbInitView implements AdbServicesInitializationPresenter.View {

    private final AdbInitProgressDialog.Factory dialogFactory;
    private final ErrorDialogs errorDialogs;

    private @Nullable AdbInitProgressDialog currentDialog;

    @Inject
    MainFrameAdbInitView(AdbInitProgressDialog.Factory dialogFactory, ErrorDialogs errorDialogs) {
        this.dialogFactory = dialogFactory;
        this.errorDialogs = errorDialogs;
    }

    @Override
    public void showAdbLoadingProgress(boolean isCancellable, Runnable userHideAction) {
        assert currentDialog == null;
        currentDialog = dialogFactory.create(isCancellable ? "Cancel" : "Hide");
        currentDialog.show(userHideAction);
    }

    @Override
    public void hideAdbLoadingProgress() {
        var currentDialog = this.currentDialog;
        if (currentDialog != null) {
            currentDialog.hide();
            this.currentDialog = null;
        }
    }

    @Override
    public void showAdbLoadingError(String failureReason) {
        hideAdbLoadingProgress();
        errorDialogs.showAdbFailedToStartError(failureReason);
    }
}

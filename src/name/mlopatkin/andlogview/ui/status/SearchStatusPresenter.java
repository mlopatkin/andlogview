/*
 * Copyright 2021 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.ui.status;

import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.utils.Cancellable;
import name.mlopatkin.andlogview.utils.UiThreadScheduler;

import com.google.common.annotations.VisibleForTesting;

import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

/**
 * A presenter for the status bar that shows most recent search status.
 */
@MainFrameScoped
public class SearchStatusPresenter {
    private static final String MESSAGE_NOT_FOUND = "Text not found";
    @VisibleForTesting
    static final int MESSAGE_SHOW_TIMEOUT_MS = 2000;

    public interface View {
        void showSearchMessage(String message);
        void hideSearchMessage();
    }

    private final View view;
    private final UiThreadScheduler timer;

    @Nullable
    private Cancellable pendingHideJob;

    @Inject
    public SearchStatusPresenter(View view, UiThreadScheduler timer) {
        this.view = view;
        this.timer = timer;
    }

    /**
     * Shows a message about text being not found. The text hides after some delay.
     */
    public void showNotFoundMessage() {
        cancelPendingHideJobIfNeeded();
        view.showSearchMessage(MESSAGE_NOT_FOUND);
        pendingHideJob = timer.postDelayedTask(this::reset, MESSAGE_SHOW_TIMEOUT_MS);
    }

    /**
     * Hides all search status messages.
     */
    public void reset() {
        cancelPendingHideJobIfNeeded();
        view.hideSearchMessage();
    }

    private void cancelPendingHideJobIfNeeded() {
        Cancellable hideJob = pendingHideJob;
        if (hideJob != null) {
            hideJob.cancel();
            pendingHideJob = null;
        }
    }
}

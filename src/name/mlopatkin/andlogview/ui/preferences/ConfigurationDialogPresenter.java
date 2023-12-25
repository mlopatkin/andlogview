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

package name.mlopatkin.andlogview.ui.preferences;

import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.ui.device.AdbServicesInitializationPresenter;
import name.mlopatkin.andlogview.ui.device.AdbServicesStatus;

import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;

public class ConfigurationDialogPresenter {
    // Design consideration: the dialog doesn't close itself on commit/discard, it waits for the presenter to take a
    // decision and call show()/hide(). This means that the presenter can abort the closing.
    interface View {
        void setAdbLocation(String adbLocation);

        String getAdbLocation();

        void setAutoReconnectEnabled(boolean enabled);

        boolean isAutoReconnectEnabled();

        void setCommitAction(Runnable runnable);

        void setDiscardAction(Runnable runnable);

        void setAdbLocationChecker(Predicate<String> locationChecker);

        void showInvalidAdbLocationError();

        void show();

        void hide();
    }

    private final View view;
    private final AdbConfigurationPref adbConfigurationPref;
    private final AdbServicesInitializationPresenter adbServicesPresenter;
    private final AdbServicesStatus adbServicesStatus;

    @Inject
    ConfigurationDialogPresenter(
            View view,
            AdbConfigurationPref adbConfigurationPref,
            AdbServicesInitializationPresenter adbServicesPresenter,
            AdbServicesStatus adbServicesStatus) {
        this.view = view;
        this.adbConfigurationPref = adbConfigurationPref;
        this.adbServicesPresenter = adbServicesPresenter;
        this.adbServicesStatus = adbServicesStatus;
    }

    public void openDialog() {
        view.setCommitAction(this::tryCommit);
        view.setDiscardAction(this::discard);
        view.setAdbLocationChecker(adbConfigurationPref::checkAdbLocation);

        view.setAdbLocation(adbConfigurationPref.getAdbLocation());
        view.setAutoReconnectEnabled(adbConfigurationPref.isAutoReconnectEnabled());
        view.show();
    }

    private void tryCommit() {
        String prevLocation = adbConfigurationPref.getAdbLocation();
        boolean hasLocationChanged = !Objects.equals(prevLocation, view.getAdbLocation());

        if (hasLocationChanged && !adbConfigurationPref.trySetAdbLocation(view.getAdbLocation())) {
            view.showInvalidAdbLocationError();
            return;
        }

        adbConfigurationPref.setAutoReconnectEnabled(view.isAutoReconnectEnabled());
        view.hide();

        if (hasLocationChanged || adbServicesStatus.getStatus().isFailed()) {
            adbServicesPresenter.restartAdb();
        }
    }

    private void discard() {
        view.hide();
    }
}

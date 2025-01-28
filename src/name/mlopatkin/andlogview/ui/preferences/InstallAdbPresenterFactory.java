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

import java.awt.Desktop;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Manages the creation of the presenter.
 */
public class InstallAdbPresenterFactory {
    private final Provider<DesktopInstallAdbPresenter> desktopInstalProvider;

    @Inject
    public InstallAdbPresenterFactory(Provider<DesktopInstallAdbPresenter> desktopInstalProvider) {
        this.desktopInstalProvider = desktopInstalProvider;
    }

    public InstallAdbPresenter createPresenter() {
        if (Desktop.isDesktopSupported()) {
            return desktopInstalProvider.get();
        }
        return DisabledInstallAdbPresenter.INSTANCE;
    }
}

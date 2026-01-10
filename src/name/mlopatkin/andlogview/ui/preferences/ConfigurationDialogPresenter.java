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

import name.mlopatkin.andlogview.AppExecutors;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.preferences.ThemeColorsPref;
import name.mlopatkin.andlogview.ui.device.AdbServicesInitializationPresenter;
import name.mlopatkin.andlogview.ui.device.AdbServicesStatus;
import name.mlopatkin.andlogview.ui.themes.CurrentTheme;
import name.mlopatkin.andlogview.ui.themes.Theme;
import name.mlopatkin.andlogview.utils.MyFutures;

import com.google.common.base.CharMatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;

public class ConfigurationDialogPresenter {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationDialogPresenter.class);

    // Design consideration: the dialog doesn't close itself on commit/discard, it waits for the presenter to take a
    // decision and call show()/hide(). This means that the presenter can abort the closing.
    interface View {
        void setThemes(String selectedTheme, List<String> availableThemes);

        String getSelectedThemeName();

        void setThemeSelectedAction(Consumer<? super String> newTheme);

        void setAdbLocation(String adbLocation);

        String getAdbLocation();

        void setAutoReconnectEnabled(boolean enabled);

        boolean isAutoReconnectEnabled();

        void setCommitAction(Runnable runnable);

        void setDiscardAction(Runnable runnable);

        void setAdbLocationChecker(Predicate<String> locationChecker);

        void showInvalidAdbLocationError(String newLocation);

        void setAdbInstallAvailable(boolean available);

        void setAdbInstallerAction(Runnable runnable);

        void show();

        void hide();
    }

    private final View view;
    private final ThemeColorsPref themePref;
    private final CurrentTheme currentTheme;
    private final AdbConfigurationPref adbConfigurationPref;
    private final AdbServicesInitializationPresenter adbServicesPresenter;
    private final AdbServicesStatus adbServicesStatus;
    private final InstallAdbPresenter adbInstaller;
    private final Executor uiExecutor;

    @Inject
    ConfigurationDialogPresenter(
            View view,
            ThemeColorsPref themePref,
            CurrentTheme currentTheme,
            AdbConfigurationPref adbConfigurationPref,
            AdbServicesInitializationPresenter adbServicesPresenter,
            AdbServicesStatus adbServicesStatus,
            InstallAdbPresenter adbInstaller,
            @Named(AppExecutors.UI_EXECUTOR) Executor uiExecutor) {
        this.view = view;
        this.themePref = themePref;
        this.currentTheme = currentTheme;
        this.adbConfigurationPref = adbConfigurationPref;
        this.adbServicesPresenter = adbServicesPresenter;
        this.adbServicesStatus = adbServicesStatus;
        this.adbInstaller = adbInstaller;
        this.uiExecutor = uiExecutor;
    }

    public void openDialog() {
        view.setCommitAction(this::tryCommit);
        view.setDiscardAction(this::discard);
        view.setAdbLocationChecker(path -> adbConfigurationPref.checkAdbLocation(sanitizeAdbLocation(path)));

        var themes = themePref.getAvailableThemes().stream().map(Theme::getDisplayName).toList();
        view.setThemes(themePref.getSelectedTheme().getDisplayName(), themes);
        view.setThemeSelectedAction(themeName -> {
            log.info("Selected theme {}", themeName);
            currentTheme.set(themePref.getAvailableThemes().stream()
                    .filter(theme -> theme.getDisplayName().equals(themeName))
                    .findAny()
                    .orElseThrow()
            );
        });

        view.setAdbLocation(adbConfigurationPref.getAdbLocation());
        view.setAutoReconnectEnabled(adbConfigurationPref.isAutoReconnectEnabled());
        view.setAdbInstallAvailable(!adbConfigurationPref.hasValidAdbLocation() && adbInstaller.isAvailable());
        view.setAdbInstallerAction(this::startAdbInstall);

        adbServicesPresenter.postponeErrorDialogs();
        view.show();
    }

    private String sanitizeAdbLocation(String location) {
        return CharMatcher.whitespace().trimFrom(location);
    }

    private void startAdbInstall() {
        adbInstaller.startInstall()
                .thenAcceptAsync(result -> {
                    log.info("ADB install completed with result={}", result);
                    if (result instanceof InstallAdbPresenter.Installed installed) {
                        view.setAdbLocation(installed.getAdbPath().getAbsolutePath());
                    }
                }, uiExecutor)
                .exceptionally(MyFutures::uncaughtException);
    }

    private void tryCommit() {
        String prevLocation = adbConfigurationPref.getAdbLocation();
        var newLocation = sanitizeAdbLocation(view.getAdbLocation());
        boolean hasLocationChanged = !Objects.equals(prevLocation, newLocation);

        if (hasLocationChanged && !adbConfigurationPref.trySetAdbLocation(newLocation)) {
            view.showInvalidAdbLocationError(newLocation);
            return;
        }

        adbConfigurationPref.setAutoReconnectEnabled(view.isAutoReconnectEnabled());
        view.hide();

        if (hasLocationChanged || adbServicesStatus.getStatus().isFailed()) {
            adbServicesPresenter.restartAdb();
        }

        // Show any postponed ADB errors if we didn't restart ADB above.
        adbServicesPresenter.resumeErrorDialogs();
    }

    private void discard() {
        view.hide();
        // Show any postponed ADB errors since the dialog closed without changes.
        adbServicesPresenter.resumeErrorDialogs();
    }
}

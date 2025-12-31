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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.FakeInMemoryConfigStorage;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.sdkrepo.AdbLocationDiscovery;
import name.mlopatkin.andlogview.test.Expectations;
import name.mlopatkin.andlogview.test.TestActionHandler;
import name.mlopatkin.andlogview.ui.device.AdbServicesInitializationPresenter;
import name.mlopatkin.andlogview.ui.device.AdbServicesStatus;
import name.mlopatkin.andlogview.utils.FakePathResolver;

import com.google.common.util.concurrent.MoreExecutors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@ExtendWith(MockitoExtension.class)
class ConfigurationDialogPresenterTest {
    private static final String DEFAULT_ADB_LOCATION = AdbLocationDiscovery.ADB_EXECUTABLE;
    private static final String VALID_ADB_LOCATION = "validAdb";
    private static final String INVALID_ADB_LOCATION = "invalidAdb";

    private final ConfigStorage configStorage = new FakeInMemoryConfigStorage();
    private final FakeView fakeView = new FakeView();

    @Mock
    private AdbServicesInitializationPresenter adbServicesInitPresenter;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private AdbServicesStatus adbServicesStatus;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private InstallAdbPresenter installPresenter;

    @BeforeEach
    void setUp() {
        when(adbServicesStatus.getStatus()).thenReturn(AdbServicesStatus.StatusValue.initialized());
        when(installPresenter.isAvailable()).thenReturn(true);
        when(installPresenter.startInstall()).thenReturn(
                CompletableFuture.completedFuture(InstallAdbPresenter.Result.manual()));
    }

    @Test
    void openDialogShowsViewWithProperLocationSet() {
        var presenter = createPresenter();

        presenter.openDialog();

        assertThat(fakeView.getAdbLocation()).isEqualTo(DEFAULT_ADB_LOCATION);
        assertThat(fakeView.isAutoReconnectEnabled()).isTrue();
        assertThat(fakeView.isShown()).isTrue();
    }

    @Test
    void committingWithInvalidLocationShowsError() {
        var presenter = createPresenter();

        presenter.openDialog();
        fakeView.selectAdbLocation(INVALID_ADB_LOCATION);

        Expectations.expect("show warning dialog", expectation -> {
            fakeView.onAdbLocationWarningShown.setAction(expectation::fulfill);
            fakeView.commit();
        });
        assertTrue(fakeView.isShown(), "configuration dialog is shown");
    }

    @Test
    void discardingWithInvalidLocationShowsNoError() {
        var presenter = createPresenter();

        presenter.openDialog();
        fakeView.selectAdbLocation(INVALID_ADB_LOCATION);

        Expectations.ensure("warning dialog wasn't shown", condition -> {
            fakeView.onAdbLocationWarningShown.setAction(condition::invalidate);
            fakeView.discard();
        });
        assertFalse(fakeView.isShown(), "configuration dialog is hidden");
    }

    @Test
    void committingWithNewLocationRestartsAdb() {
        var presenter = createPresenter();

        presenter.openDialog();
        fakeView.selectAdbLocation(VALID_ADB_LOCATION);

        fakeView.commit();

        assertFalse(fakeView.isShown(), "configuration dialog is hidden");
        verify(adbServicesInitPresenter).restartAdb();
    }

    @Test
    void committingAfterChangingAutoReconnectUpdatesPreference() {
        var presenter = createPresenter();

        presenter.openDialog();
        fakeView.setAutoReconnectEnabled(false);
        fakeView.commit();

        assertThat(adbConfiguration().isAutoReconnectEnabled()).isFalse();
    }

    @Test
    void settingInvalidAdbLocationHighlightError() {
        var presenter = createPresenter();

        presenter.openDialog();
        fakeView.selectAdbLocation(INVALID_ADB_LOCATION);
        assertTrue(fakeView.isInvalidAdbLocationHighlighted());
    }

    @Test
    void settingValidAdbLocationClearsError() {
        var presenter = createPresenter();

        presenter.openDialog();
        fakeView.selectAdbLocation(INVALID_ADB_LOCATION);
        assumeTrue(fakeView.isInvalidAdbLocationHighlighted());

        fakeView.selectAdbLocation(VALID_ADB_LOCATION);
        assertFalse(fakeView.isInvalidAdbLocationHighlighted());
    }

    @Test
    void committingRestartsAdbWithoutChangesIfItWasNotRunning() {
        withValidAdbLocation();
        when(adbServicesStatus.getStatus()).thenReturn(AdbServicesStatus.StatusValue.failed("Not initialized"));

        var presenter = createPresenter();

        presenter.openDialog();
        fakeView.commit();

        verify(adbServicesInitPresenter).restartAdb();
    }

    @Test
    void adbInstallIsNotAvailableWhenLocationIsValid() {
        withValidAdbLocation();

        var presenter = createPresenter();
        presenter.openDialog();

        assertThat(fakeView.isAdbInstallAvailable()).isFalse();
    }

    @Test
    void adbInstallIsAvailableWhenLocationIsInvalid() {
        withInvalidAdbLocation();

        var presenter = createPresenter();
        presenter.openDialog();

        assertThat(fakeView.isAdbInstallAvailable()).isTrue();
    }

    @Test
    void adbInstallationStartsWhenUserSelects() {
        withInvalidAdbLocation();

        var presenter = createPresenter();
        presenter.openDialog();
        fakeView.requestAdbInstall();

        verify(installPresenter).startInstall();
    }

    @Test
    void successfulAdbInstallSetsLocationToExecutablePath() {
        withInvalidAdbLocation();
        File adbExecutable = new File("/home/user/.logview/android-sdk/platform-tools/adb");
        when(installPresenter.startInstall()).thenReturn(
                CompletableFuture.completedFuture(InstallAdbPresenter.Result.installed(adbExecutable)));

        var presenter = createPresenter();
        presenter.openDialog();
        fakeView.requestAdbInstall();

        assertThat(fakeView.getAdbLocation()).isEqualTo(adbExecutable.getAbsolutePath());
    }

    @Test
    void cancelledAdbInstallDoesNotChangeLocation() {
        withInvalidAdbLocation();
        when(installPresenter.startInstall()).thenReturn(
                CompletableFuture.completedFuture(InstallAdbPresenter.Result.cancelled()));

        var presenter = createPresenter();
        presenter.openDialog();
        String originalLocation = fakeView.getAdbLocation();
        fakeView.requestAdbInstall();

        assertThat(fakeView.getAdbLocation()).isEqualTo(originalLocation);
    }

    @Test
    void failedAdbInstallDoesNotChangeLocation() {
        withInvalidAdbLocation();
        when(installPresenter.startInstall()).thenReturn(
                CompletableFuture.failedFuture(new RuntimeException("Network error")));

        var presenter = createPresenter();
        presenter.openDialog();
        String originalLocation = fakeView.getAdbLocation();
        fakeView.requestAdbInstall();

        assertThat(fakeView.getAdbLocation()).isEqualTo(originalLocation);
    }

    private ConfigurationDialogPresenter createPresenter() {
        return new ConfigurationDialogPresenter(fakeView, adbConfiguration(), adbServicesInitPresenter,
                adbServicesStatus, installPresenter, MoreExecutors.directExecutor());
    }

    private AdbConfigurationPref adbConfiguration() {
        return new AdbConfigurationPref(
                configStorage,
                FakePathResolver.withValidPaths(VALID_ADB_LOCATION, DEFAULT_ADB_LOCATION)
        );
    }

    private void withValidAdbLocation() {
        withAdbLocation(VALID_ADB_LOCATION);
    }

    private void withInvalidAdbLocation() {
        withAdbLocation(INVALID_ADB_LOCATION);
    }

    private void withAdbLocation(String adbLocation) {
        // To avoid setting the location in the storage directly, we create a temp pref instance that will accept
        // the location regardless of what adbConfiguration()-provided instance thinks.
        var forceAcceptingLocation =
                new AdbConfigurationPref(configStorage, FakePathResolver.withValidPaths(adbLocation));
        if (!forceAcceptingLocation.trySetAdbLocation(adbLocation)) {
            throw new AssertionError("Could not set adb location");
        }
    }

    static class FakeView implements ConfigurationDialogPresenter.View {
        private final TestActionHandler<Runnable> onCommit = TestActionHandler.runnableAction();
        private final TestActionHandler<Runnable> onDiscard = TestActionHandler.runnableAction();
        private final TestActionHandler<Predicate<String>> checkAdbLocation = TestActionHandler.predicateAction(true);
        private final TestActionHandler<Runnable> onAdbInstall = TestActionHandler.runnableAction();

        final TestActionHandler<Runnable> onAdbLocationWarningShown = TestActionHandler.runnableAction();

        private String adbLocation = "";
        private boolean isShown;
        private boolean enableAutoReconnect;
        private boolean isInvalidAdbLocationHighlighted;
        private boolean isAdbInstallAvailable;

        public boolean isShown() {
            return isShown;
        }

        public boolean isInvalidAdbLocationHighlighted() {
            return isInvalidAdbLocationHighlighted;
        }

        public boolean isAdbInstallAvailable() {
            return isAdbInstallAvailable;
        }

        public void commit() {
            onCommit.action().run();
        }

        public void discard() {
            onDiscard.action().run();
        }

        public void requestAdbInstall() {
            assertThat(isAdbInstallAvailable).isTrue();
            onAdbInstall.action().run();
        }

        public void selectAdbLocation(String location) {
            adbLocation = location;
            isInvalidAdbLocationHighlighted = !checkAdbLocation.action().test(location);
        }

        @Override
        public void setAdbLocation(String adbLocation) {
            this.adbLocation = adbLocation;
        }

        @Override
        public String getAdbLocation() {
            return adbLocation;
        }

        @Override
        public void setAutoReconnectEnabled(boolean enabled) {
            enableAutoReconnect = enabled;
        }

        @Override
        public boolean isAutoReconnectEnabled() {
            return enableAutoReconnect;
        }

        @Override
        public void setCommitAction(Runnable runnable) {
            onCommit.setAction(Objects.requireNonNull(runnable));
        }

        @Override
        public void setDiscardAction(Runnable runnable) {
            onDiscard.setAction(Objects.requireNonNull(runnable));
        }

        @Override
        public void setAdbLocationChecker(Predicate<String> locationChecker) {
            checkAdbLocation.setAction(locationChecker);
        }

        @Override
        public void setAdbInstallAvailable(boolean available) {
            isAdbInstallAvailable = available;
        }

        @Override
        public void setAdbInstallerAction(Runnable runnable) {
            onAdbInstall.setAction(runnable);
        }

        @Override
        public void show() {
            isShown = true;
        }

        @Override
        public void hide() {
            isShown = false;
        }

        @Override
        public void showInvalidAdbLocationError(String newLocation) {
            onAdbLocationWarningShown.action().run();
        }
    }
}

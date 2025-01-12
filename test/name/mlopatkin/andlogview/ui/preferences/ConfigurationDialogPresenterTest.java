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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.config.FakeInMemoryConfigStorage;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.test.Expectations;
import name.mlopatkin.andlogview.test.TestActionHandler;
import name.mlopatkin.andlogview.ui.device.AdbServicesInitializationPresenter;
import name.mlopatkin.andlogview.ui.device.AdbServicesStatus;
import name.mlopatkin.andlogview.utils.SystemPathResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@ExtendWith(MockitoExtension.class)
class ConfigurationDialogPresenterTest {

    private static final String DEFAULT_ADB_LOCATION = "adb";
    private static final String VALID_ADB_LOCATION = "validAdb";
    private static final String INVALID_ADB_LOCATION = "invalidAdb";


    private static final boolean DEFAULT_AUTO_RECONNECT = false;
    private static final boolean CHANGED_AUTO_RECONNECT = true;

    private final FakeView fakeView = new FakeView();

    private final AdbConfigurationPref adbConfiguration = new AdbConfigurationPref(new FakeInMemoryConfigStorage(),
            new FakePathResolver(VALID_ADB_LOCATION, DEFAULT_ADB_LOCATION));

    @Mock
    private AdbServicesInitializationPresenter adbServicesInitPresenter;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private AdbServicesStatus adbServicesStatus;

    @BeforeEach
    void setUp() {
        adbConfiguration.setAutoReconnectEnabled(DEFAULT_AUTO_RECONNECT);
        adbConfiguration.trySetAdbLocation(DEFAULT_ADB_LOCATION);

        when(adbServicesStatus.getStatus()).thenReturn(AdbServicesStatus.StatusValue.initialized());
    }

    @Test
    void openDialogShowsViewWithProperLocationSet() {
        var presenter = createPresenter();

        presenter.openDialog();

        assertEquals(DEFAULT_ADB_LOCATION, fakeView.getAdbLocation());
        assertFalse(fakeView.isAutoReconnectEnabled());
        assertTrue(fakeView.isShown());
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
        fakeView.setAutoReconnectEnabled(CHANGED_AUTO_RECONNECT);
        fakeView.commit();

        assertEquals(CHANGED_AUTO_RECONNECT, adbConfiguration.isAutoReconnectEnabled());
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
        adbConfiguration.trySetAdbLocation(VALID_ADB_LOCATION);
        when(adbServicesStatus.getStatus()).thenReturn(AdbServicesStatus.StatusValue.failed("Not initialized"));

        var presenter = createPresenter();

        presenter.openDialog();
        fakeView.commit();

        verify(adbServicesInitPresenter).restartAdb();
    }

    private ConfigurationDialogPresenter createPresenter() {
        return new ConfigurationDialogPresenter(fakeView, adbConfiguration, adbServicesInitPresenter,
                adbServicesStatus);
    }

    static class FakeView implements ConfigurationDialogPresenter.View {
        private final TestActionHandler<Runnable> onCommit = TestActionHandler.runnableAction();
        private final TestActionHandler<Runnable> onDiscard = TestActionHandler.runnableAction();
        private final TestActionHandler<Predicate<String>> checkAdbLocation = TestActionHandler.predicateAction(true);

        final TestActionHandler<Runnable> onAdbLocationWarningShown = TestActionHandler.runnableAction();

        private String adbLocation = "";
        private boolean isShown;
        private boolean enableAutoReconnect;
        private boolean isInvalidAdbLocationHighlighted;

        public boolean isShown() {
            return isShown;
        }

        public boolean isInvalidAdbLocationHighlighted() {
            return isInvalidAdbLocationHighlighted;
        }

        public void commit() {
            onCommit.action().run();
        }

        public void discard() {
            onDiscard.action().run();
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
        public void show() {
            isShown = true;
        }

        @Override
        public void hide() {
            isShown = false;
        }

        @Override
        public void showInvalidAdbLocationError() {
            onAdbLocationWarningShown.action().run();
        }

    }

    private static class FakePathResolver extends SystemPathResolver {
        private final Set<String> validPaths = new HashSet<>();

        FakePathResolver(String... validPaths) {
            this.validPaths.addAll(Arrays.asList(validPaths));
        }

        @Override
        public Optional<File> resolveExecutablePath(String rawPath) {
            if (validPaths.contains(rawPath)) {
                return Optional.of(new File(rawPath));
            }
            return Optional.empty();
        }
    }
}

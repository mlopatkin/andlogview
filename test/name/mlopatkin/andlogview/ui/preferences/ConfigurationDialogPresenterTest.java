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

import name.mlopatkin.andlogview.config.FakeInMemoryConfigStorage;
import name.mlopatkin.andlogview.preferences.AdbConfigurationPref;
import name.mlopatkin.andlogview.test.Expectations;
import name.mlopatkin.andlogview.test.TestActionHandler;
import name.mlopatkin.andlogview.utils.SystemPathResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

class ConfigurationDialogPresenterTest {

    private static final String DEFAULT_ADB_LOCATION = "adb";
    private static final String VALID_ADB_LOCATION = "validAdb";
    private static final String INVALID_ADB_LOCATION = "invalidAdb";


    private static final boolean DEFAULT_AUTO_RECONNECT = false;
    private static final boolean CHANGED_AUTO_RECONNECT = true;

    private final FakeView fakeView = new FakeView();

    private final AdbConfigurationPref adbConfiguration = new AdbConfigurationPref(new FakeInMemoryConfigStorage(),
            new FakePathResolver(VALID_ADB_LOCATION));

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        adbConfiguration.setAutoReconnectEnabled(DEFAULT_AUTO_RECONNECT);
        adbConfiguration.setAdbLocation(DEFAULT_ADB_LOCATION);
    }

    @Test
    void openDialogShowsViewWithProperLocationSet() {
        ConfigurationDialogPresenter presenter = new ConfigurationDialogPresenter(fakeView, adbConfiguration);

        presenter.openDialog();

        assertEquals(DEFAULT_ADB_LOCATION, fakeView.getAdbLocation());
        assertFalse(fakeView.isAutoReconnectEnabled());
        assertTrue(fakeView.isShown());
    }

    @Test
    void committingWithInvalidLocationShowsError() {
        ConfigurationDialogPresenter presenter = new ConfigurationDialogPresenter(fakeView, adbConfiguration);

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
        ConfigurationDialogPresenter presenter = new ConfigurationDialogPresenter(fakeView, adbConfiguration);

        presenter.openDialog();
        fakeView.selectAdbLocation(INVALID_ADB_LOCATION);

        Expectations.ensure("warning dialog wasn't shown", condition -> {
            fakeView.onAdbLocationWarningShown.setAction(condition::invalidate);
            fakeView.discard();
        });
        assertFalse(fakeView.isShown(), "configuration dialog is hidden");
    }

    @Test
    void committingWithNewLocationShowsRestartWarning() {
        ConfigurationDialogPresenter presenter = new ConfigurationDialogPresenter(fakeView, adbConfiguration);

        presenter.openDialog();
        fakeView.selectAdbLocation(VALID_ADB_LOCATION);

        Expectations.expect("show restart warning dialog", expectation -> {
            fakeView.onRestartWarningShown.setAction(expectation::fulfill);
            fakeView.commit();
        });
        assertFalse(fakeView.isShown(), "configuration dialog is hidden");
    }

    @Test
    void committingAfterChangingAutoReconnectUpdatesPreference() {
        ConfigurationDialogPresenter presenter = new ConfigurationDialogPresenter(fakeView, adbConfiguration);

        presenter.openDialog();
        fakeView.setAutoReconnectEnabled(CHANGED_AUTO_RECONNECT);
        fakeView.commit();

        assertEquals(CHANGED_AUTO_RECONNECT, adbConfiguration.isAutoReconnectEnabled());
    }

    static class FakeView implements ConfigurationDialogPresenter.View {
        private final TestActionHandler<Runnable> onCommit = TestActionHandler.runnableAction();
        private final TestActionHandler<Runnable> onDiscard = TestActionHandler.runnableAction();

        final TestActionHandler<Runnable> onAdbLocationWarningShown = TestActionHandler.runnableAction();
        final TestActionHandler<Runnable> onRestartWarningShown = TestActionHandler.runnableAction();

        private String adbLocation;
        private boolean isShown;
        private boolean enableAutoReconnect;

        public boolean isShown() {
            return isShown;
        }

        public void commit() {
            onCommit.action().run();
        }

        public void discard() {
            onDiscard.action().run();
        }

        public void selectAdbLocation(String location) {
            adbLocation = location;
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

        @Override
        public void showRestartAppWarning() {
            onRestartWarningShown.action().run();
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

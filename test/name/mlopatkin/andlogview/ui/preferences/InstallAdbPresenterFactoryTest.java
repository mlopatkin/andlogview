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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.ui.preferences.InstallAdbPresenter.Cancelled;
import name.mlopatkin.andlogview.ui.preferences.InstallAdbPresenter.Installed;
import name.mlopatkin.andlogview.ui.preferences.InstallAdbPresenter.ManualFallback;
import name.mlopatkin.andlogview.ui.preferences.InstallAdbPresenter.Result;
import name.mlopatkin.andlogview.utils.MyFutures;

import com.google.common.util.concurrent.MoreExecutors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.inject.Provider;

@ExtendWith(MockitoExtension.class)
class InstallAdbPresenterFactoryTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    private Provider<DesktopInstallAdbPresenter> desktopProvider;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private Provider<DownloadAdbPresenter> downloadProvider;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private DesktopInstallAdbPresenter desktopPresenter;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private DownloadAdbPresenter downloadPresenter;

    private final Executor uiExecutor = MoreExecutors.directExecutor();
    private InstallAdbPresenter presenter;

    @BeforeEach
    void setUp() {
        when(desktopProvider.get()).thenReturn(desktopPresenter);
        when(downloadProvider.get()).thenReturn(downloadPresenter);

        var factory = new InstallAdbPresenterFactory(true, desktopProvider, downloadProvider, uiExecutor);
        presenter = factory.createPresenter();
    }

    @ParameterizedTest(name = "downloadAvailable={0}, desktopAvailable={1}, expected={2}")
    @CsvSource({
            "true, false, true",   // Only download available
            "false, true, true",   // Only desktop available
            "true, true, true",    // Both available
            "false, false, false"  // Neither available
    })
    void isAvailableReturnsCorrectValue(boolean downloadAvailable, boolean desktopAvailable, boolean expected) {
        when(downloadPresenter.isAvailable()).thenReturn(downloadAvailable);
        when(desktopPresenter.isAvailable()).thenReturn(desktopAvailable);

        assertThat(presenter.isAvailable()).isEqualTo(expected);
    }

    @Nested
    class StartInstallTests {
        private void setupDownloadPresenter(Result downloadResult) {
            when(downloadPresenter.isAvailable()).thenReturn(true);
            when(downloadPresenter.startInstall()).thenReturn(CompletableFuture.completedFuture(downloadResult));
        }

        private void setupDesktopPresenter(Result desktopResult) {
            when(desktopPresenter.startInstall()).thenReturn(CompletableFuture.completedFuture(desktopResult));
        }

        @Test
        void startInstallUsesDownloadPresenterWhenAvailable() throws Exception {
            File adbPath = new File("/path/to/adb");
            setupDownloadPresenter(Result.installed(adbPath));

            var result = presenter.startInstall().get();

            assertThat(result).isInstanceOf(Installed.class);
            assertThat(((Installed) result).getAdbPath()).isEqualTo(adbPath);
            verify(desktopPresenter, never()).startInstall();
        }

        @Test
        void startInstallFailsWhenDownloadFails() {
            RuntimeException exception = new RuntimeException("Network error");
            when(downloadPresenter.isAvailable()).thenReturn(true);
            when(downloadPresenter.startInstall()).thenReturn(MyFutures.failedFuture(exception));

            var future = presenter.startInstall();

            assertThat(future).isCompletedExceptionally();
            verify(desktopPresenter, never()).startInstall();
        }

        @Test
        void startInstallReturnsCancelledWhenDownloadIsCancelled() throws Exception {
            setupDownloadPresenter(Result.cancelled());

            var result = presenter.startInstall().get();

            assertThat(result).isInstanceOf(Cancelled.class);
            verify(desktopPresenter, never()).startInstall();
        }

        @Test
        void startInstallFallsBackToDesktopWhenDownloadReturnsManualFallback() throws Exception {
            setupDownloadPresenter(Result.manual());
            setupDesktopPresenter(Result.manual());

            var result = presenter.startInstall().get();

            assertThat(result).isInstanceOf(ManualFallback.class);
        }

        @Test
        void startInstallFallsBackToDesktopWhenDownloadReturnsPackageNotFound() throws Exception {
            setupDownloadPresenter(Result.notFound());
            setupDesktopPresenter(Result.manual());

            var result = presenter.startInstall().get();

            assertThat(result).isInstanceOf(ManualFallback.class);
        }

        @Test
        void startInstallUsesDesktopPresenterWhenDownloadPresenterNotAvailable() throws Exception {
            when(downloadPresenter.isAvailable()).thenReturn(false);
            when(desktopPresenter.isAvailable()).thenReturn(true);
            setupDesktopPresenter(Result.manual());

            var result = presenter.startInstall().get();

            assertThat(result).isInstanceOf(ManualFallback.class);
        }

        @Test
        void startInstallFailsWhenDesktopPresenterFailsAsOnlyAvailablePresenter() {
            RuntimeException exception = new RuntimeException("Failed to open browser");
            when(downloadPresenter.isAvailable()).thenReturn(false);
            when(desktopPresenter.isAvailable()).thenReturn(true);
            when(desktopPresenter.startInstall()).thenReturn(MyFutures.failedFuture(exception));

            var future = presenter.startInstall();

            assertThat(future).isCompletedExceptionally();
        }

        @Test
        void startInstallFailsWhenDesktopPresenterFailsAfterManualFallback() {
            RuntimeException exception = new RuntimeException("Failed to open browser");
            setupDownloadPresenter(Result.manual());
            when(desktopPresenter.startInstall()).thenReturn(MyFutures.failedFuture(exception));

            var future = presenter.startInstall();

            assertThat(future).isCompletedExceptionally();
        }

        @Test
        void startInstallFailsWhenDesktopPresenterFailsAfterPackageNotFoundFallback() {
            RuntimeException exception = new RuntimeException("Failed to open browser");
            setupDownloadPresenter(Result.notFound());
            when(desktopPresenter.startInstall()).thenReturn(MyFutures.failedFuture(exception));

            var future = presenter.startInstall();

            assertThat(future).isCompletedExceptionally();
        }

        @Test
        void startInstallFailsWhenNeitherPresenterIsAvailable() {
            when(downloadPresenter.isAvailable()).thenReturn(false);
            when(desktopPresenter.isAvailable()).thenReturn(false);

            var future = presenter.startInstall();

            assertThat(future).isCompletedExceptionally();
        }
    }
}

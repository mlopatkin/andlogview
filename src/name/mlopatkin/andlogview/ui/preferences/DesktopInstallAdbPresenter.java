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

import name.mlopatkin.andlogview.utils.MyFutures;

import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class DesktopInstallAdbPresenter implements InstallAdbPresenter {
    private static final Logger logger = LoggerFactory.getLogger(DesktopInstallAdbPresenter.class);

    @Inject
    public DesktopInstallAdbPresenter() {
        Preconditions.checkState(Desktop.isDesktopSupported(), "Desktop is not supported, cannot use it");
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public CompletableFuture<Result> startInstall() {
        try {
            // TODO(mlopatkin) should I go through andlogview.mlopatkin.name to give a chance to redirect if the url
            //  changes?
            //  Probably should do a quick ping first, to make sure my site is still available.
            Desktop.getDesktop()
                    .browse(URI.create("https://developer.android.com/tools/releases/platform-tools#downloads"));
            return CompletableFuture.completedFuture(Result.manual());
        } catch (IOException e) {
            logger.error("Failed to open the default browser", e);
            return MyFutures.failedFuture(e);
        }
    }
}

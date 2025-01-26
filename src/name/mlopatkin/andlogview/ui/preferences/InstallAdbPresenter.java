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

/**
 * Goes through ADB install flow. It is planned to have several steps:
 * <ol>
 *     <li>Download the Android SDK package list</li>
 *     <li>Ask the user to accept the SDK license and select the destination directory</li>
 *     <li>Download and extract the package</li>
 *     <li>Configure AndLogView to use the extracted ADB</li>
 * </ol>
 * <p>
 * Alternatively, when these niceties aren't available, just open the browser for the user to download the Platform
 * tools.
 */
public interface InstallAdbPresenter {
    boolean isAvailable();

    void startInstall();
}

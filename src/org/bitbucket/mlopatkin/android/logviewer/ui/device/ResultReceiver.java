/*
 * Copyright 2017 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.device;

import com.android.ddmlib.IDevice;

/**
 * Callback interface for receiving selected device from a SelectDeviceDialog.
 */
public interface ResultReceiver {
    /**
     * This callback is invoked when a dialog is closed.
     *
     * @param dialog the dialog that triggered a callback
     * @param selectedDevice the selected device or null if the user cancelled the dialog
     */
    void onDialogResult(SelectDeviceDialog dialog, IDevice selectedDevice);
}

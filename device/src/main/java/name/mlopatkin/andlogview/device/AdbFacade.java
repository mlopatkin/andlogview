/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.device;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;

/**
 * A facade for the {@link AndroidDebugBridge}. Main purpose is to facilitate testing.
 */
interface AdbFacade {
    interface AdbBridgeObserver {
        void onAdbBridgeClosed();
    }

    IDevice[] getDevices();

    void addDeviceChangeListener(AndroidDebugBridge.IDeviceChangeListener deviceChangeListener);

    void removeDeviceChangeListener(AndroidDebugBridge.IDeviceChangeListener deviceChangeListener);

    void addBridgeObserver(AdbBridgeObserver observer);

    void removeBridgeObserver(AdbBridgeObserver observer);
}

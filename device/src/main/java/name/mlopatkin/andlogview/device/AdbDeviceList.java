/*
 * Copyright 2022 Mikhail Lopatkin
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

import name.mlopatkin.andlogview.utils.events.Observable;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Live view of the connected Android devices. Added or removed devices change the contents of the list. Iterating the
 * list creates a snapshot of currently connected devices. Use {@link AdbServer#getDeviceList(Executor)} to obtain an
 * instance of the list.
 * <p>
 * The contents of the list can be changed in background, make sure to start observing it before obtaining contents, or
 * some update notifications may be lost.
 * <p>
 * This list is not thread-safe and has to be used on the same executor that was used to obtain its instance.
 */
public interface AdbDeviceList extends AutoCloseable {
    // TODO(mlopatkin) add some consistency between getDevices and observers. Maybe introducing executor-confined
    //  snapshotting would be enough?
    //  This inconsistency causes a race in the DeviceListModel - it can see provisional device when initializing and
    //  receive provisionalDeviceNotification for this device later.
    /**
     * @return the list of currently connected and provisioned devices
     */
    List<Device> getDevices();

    /**
     * Returns the list of all connected devices, including not yet provisioned ones
     *
     * @return the list of all connected devices
     */
    List<ProvisionalDevice> getAllDevices();

    /**
     * @return the observable to register observers on this list
     */
    Observable<DeviceChangeObserver> asObservable();

    /**
     * Frees resources of this list.
     */
    @Override
    void close();
}

/*
 * Copyright 2020 Mikhail Lopatkin
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

/**
 * The API to work with an Android device that is connected via ADB.
 * <h2>Architectural overview</h2>
 * <p>
 * The entry point to the package is the {@link name.mlopatkin.andlogview.device.AdbManager} class. Typically, you
 * don't
 * want to have more than one in the app, as the underlying DDMLIB library is full of singletons anyway. The DDMLIB is
 * initialized on demand, when the user requests the {@link name.mlopatkin.andlogview.device.AdbServer} instance.
 * <p>
 * The server encapsulates an active connection to the ADB server. It provides the way to get a list of active devices
 * and monitor it.
 * <h3>Device provisioning</h3>
 * <p>
 * When a device is connected to the host and the ADB server detects this, it sends a notification to the clients. The
 * connected device may not be fully usable though, as it can be awaiting authorization or just setting up things (be
 * offline). Eventually it comes online and the clients can ask device to execute commands and do other stuff.
 * <p>
 * This library employs a two-step approach. The connected device is first exposed as a
 * {@link name.mlopatkin.andlogview.device.ProvisionalDevice}. This interface offers a limited functionality and
 * provides little information about device - only things that doesn't require actually fetching data from device.
 * The devices that go online are then "provisioned": the set of essential device properties is retrieved and cached.
 * After provisioning, the device is exposed as {@link name.mlopatkin.andlogview.device.Device} and the full
 * functionality is available. The provisioned device can still go offline or become disconnected, of course. However,
 * the essential metadata remains available.
 * <p>
 * With provisioning, clients of this library don't have to deal with the async API of property retrieval and can rely
 * on metadata being always available immediately.
 */
@NullMarked
package name.mlopatkin.andlogview.device;

// Implementation notes
//
// This package should be the only one that talks to the DDMLIB. Everything else should use this package's AdbDevice
// instead.

import org.jspecify.annotations.NullMarked;

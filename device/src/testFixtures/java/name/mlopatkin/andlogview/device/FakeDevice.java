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

package name.mlopatkin.andlogview.device;

import name.mlopatkin.andlogview.test.StrictMock;
import name.mlopatkin.andlogview.thirdparty.device.AndroidVersionCodes;
import name.mlopatkin.andlogview.utils.events.ThreadSafeObservable;
import name.mlopatkin.andlogview.utils.events.ThreadSafeSubject;

import com.android.ddmlib.IDevice;

import java.util.List;

public class FakeDevice implements Device {
    private final DeviceKey key = DeviceKey.of(StrictMock.strictMock(IDevice.class));
    private final String buildFingerprint;
    private final String product;
    private final String apiString;

    public FakeDevice(String buildFingerprint, String product, String apiString) {
        this.buildFingerprint = buildFingerprint;
        this.product = product;
        this.apiString = apiString;
    }

    @Override
    public DeviceKey getDeviceKey() {
        return key;
    }

    @Override
    public String getSerialNumber() {
        return String.format("%08x", System.identityHashCode(this));
    }

    @Override
    public String getName() {
        return product;
    }

    @Override
    public String getDisplayName() {
        return getName() + " " + getSerialNumber();
    }

    @Override
    public String getProduct() {
        return product;
    }

    @Override
    public String getBuildFingerprint() {
        return buildFingerprint;
    }

    @Override
    public String getApiString() {
        return apiString;
    }

    @Override
    public int getApiLevel() {
        return AndroidVersionCodes.TIRAMISU;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public Command command(List<String> commandLine) {
        return onCommand(commandLine);
    }

    @Override
    public ThreadSafeObservable<DeviceChangeObserver> asObservable() {
        // TODO(mlopatkin) implement it?
        return new ThreadSafeSubject<DeviceChangeObserver>().asObservable();
    }

    protected FakeCommand onCommand(List<String> commandLine) {
        return new FakeCommand();
    }
}

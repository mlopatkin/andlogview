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

package name.mlopatkin.andlogview.ui.device;

import name.mlopatkin.andlogview.liblogcat.ddmlib.AdbDeviceManager;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;

import org.apache.log4j.Logger;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

class DeviceListModel extends AbstractListModel<IDevice> implements AndroidDebugBridge.IDeviceChangeListener {
    private static final Logger logger = Logger.getLogger(DeviceListModel.class);

    private final AdbDeviceManager adbDeviceManager;
    private final List<IDevice> devices;

    private DeviceListModel(AdbDeviceManager adbDeviceManager) {
        this.adbDeviceManager = adbDeviceManager;
        devices = new ArrayList<>(adbDeviceManager.getAvailableDevices());
    }

    @Override
    public int getSize() {
        return devices.size();
    }

    @Override
    public IDevice getElementAt(int index) {
        return devices.get(index);
    }

    private void addDevice(IDevice device) {
        logger.debug("device added " + device);
        devices.add(device);
        fireIntervalAdded(this, devices.size() - 1, devices.size() - 1);
    }

    private void removeDevice(IDevice device) {
        logger.debug("device removed " + device);
        int index = devices.indexOf(device);
        if (index >= 0) {
            devices.remove(index);
            fireIntervalRemoved(this, index, index);
        }
    }

    @Override
    public void deviceConnected(final IDevice device) {
        logger.debug("Device connected: " + device);
        EventQueue.invokeLater(() -> addDevice(device));
    }

    @Override
    public void deviceDisconnected(final IDevice device) {
        logger.debug("Device disconnected: " + device);
        EventQueue.invokeLater(() -> removeDevice(device));
    }

    private void updateState(int changeMask) {
        if ((changeMask & (IDevice.CHANGE_STATE | IDevice.CHANGE_BUILD_INFO)) != 0) {
            fireContentsChanged(DeviceListModel.this, 0, devices.size() - 1);
        }
    }

    @Override
    public void deviceChanged(final IDevice device, final int changeMask) {
        logger.debug("Device changed: " + device + " changeMask=" + Integer.toHexString(changeMask));
        EventQueue.invokeLater(() -> updateState(changeMask));
    }

    public int getFirstOnlineDeviceIndex() {
        for (int i = 0; i < devices.size(); ++i) {
            IDevice device = devices.get(i);
            if (device.isOnline()) {
                return i;
            }
        }
        return -1;
    }

    public void unsubscribe() {
        adbDeviceManager.removeDeviceChangeListener(this);
    }

    public static DeviceListModel create(AdbDeviceManager adbDeviceManager) {
        DeviceListModel model = new DeviceListModel(adbDeviceManager);
        adbDeviceManager.addDeviceChangeListener(model);
        return model;
    }
}

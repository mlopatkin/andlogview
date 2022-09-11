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

import name.mlopatkin.andlogview.device.AdbDeviceList;
import name.mlopatkin.andlogview.device.Device;
import name.mlopatkin.andlogview.device.DeviceChangeObserver;
import name.mlopatkin.andlogview.device.ProvisionalDevice;

import com.google.common.collect.Iterables;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractListModel;

class DeviceListModel extends AbstractListModel<ProvisionalDevice> implements DeviceChangeObserver {
    private static final Logger logger = Logger.getLogger(DeviceListModel.class);

    private final AdbDeviceList adbDeviceList;
    private final List<ProvisionalDevice> devices;

    private DeviceListModel(AdbDeviceList adbDeviceList) {
        this.adbDeviceList = adbDeviceList;
        devices = new ArrayList<>(adbDeviceList.getAllDevices());
    }

    @Override
    public int getSize() {
        return devices.size();
    }

    @Override
    public ProvisionalDevice getElementAt(int index) {
        return devices.get(index);
    }

    @Override
    public void onProvisionalDeviceConnected(ProvisionalDevice device) {
        logger.debug("provisional device added " + device);
        assert findIndexOfDevice(device) < 0;
        devices.add(device);
        fireIntervalAdded(this, devices.size() - 1, devices.size() - 1);
    }

    @Override
    public void onDeviceConnected(Device device) {
        logger.debug("device provisioned " + device);
        int index = findIndexOfDevice(device);
        if (index >= 0) {
            devices.set(index, device);
            fireContentsChanged(this, index, index);
        } else {
            devices.add(device);
            fireIntervalAdded(this, devices.size() - 1, devices.size() - 1);
        }
    }

    @Override
    public void onDeviceDisconnected(ProvisionalDevice device) {
        logger.debug("device removed " + device);
        int index = findIndexOfDevice(device);
        if (index >= 0) {
            devices.remove(index);
            fireIntervalRemoved(this, index, index);
        }
    }

    @Override
    public void onDeviceChanged(Device device) {
        logger.debug("Device changed: " + device);
        int index = findIndexOfDevice(device);
        assert index >= 0;
        fireContentsChanged(DeviceListModel.this, index, index);
    }

    private int findIndexOfDevice(ProvisionalDevice device) {
        return Iterables.indexOf(devices, d -> Objects.equals(d.getDeviceKey(), device.getDeviceKey()));
    }

    public int getFirstOnlineDeviceIndex() {
        for (int i = 0; i < devices.size(); ++i) {
            ProvisionalDevice device = devices.get(i);
            if ((device instanceof Device) && ((Device) device).isOnline()) {
                return i;
            }
        }
        return -1;
    }

    public void unsubscribe() {
        adbDeviceList.asObservable().removeObserver(this);
    }

    public static DeviceListModel create(AdbDeviceList adbDeviceList) {
        DeviceListModel model = new DeviceListModel(adbDeviceList);
        adbDeviceList.asObservable().addObserver(model);
        return model;
    }
}

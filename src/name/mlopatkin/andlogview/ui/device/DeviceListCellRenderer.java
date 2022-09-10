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

import name.mlopatkin.andlogview.device.AdbDevice;
import name.mlopatkin.andlogview.device.ProvisionalAdbDevice;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class DeviceListCellRenderer implements ListCellRenderer<ProvisionalAdbDevice> {
    private final DefaultListCellRenderer cellRenderer = new DefaultListCellRenderer();

    @Override
    public Component getListCellRendererComponent(
            JList<? extends ProvisionalAdbDevice> jList, ProvisionalAdbDevice device, int index, boolean selected,
            boolean focused) {
        return cellRenderer.getListCellRendererComponent(jList, getDeviceDisplayName(device), index, selected, focused);
    }

    private String getDeviceDisplayName(ProvisionalAdbDevice device) {
        String deviceName = device.getDisplayName();
        if (device instanceof AdbDevice) {
            AdbDevice adbDevice = (AdbDevice) device;
            if (adbDevice.isOnline()) {
                return deviceName;
            }
        }
        return formatOfflineDevice(deviceName);
    }

    private String formatOfflineDevice(String deviceName) {
        return "<html><i> Offline (" + deviceName + ")</i></html>";
    }
}

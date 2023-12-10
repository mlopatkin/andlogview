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

import static name.mlopatkin.andlogview.device.AdbDeviceMatchers.hasSerial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import name.mlopatkin.andlogview.test.StrictMock;

import com.android.ddmlib.IDevice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;

import java.util.concurrent.CompletableFuture;

class DispatchingDeviceListTest {
    private final FakeAdbFacade adbFacade = new FakeAdbFacade();

    @Test
    void initialListOfDevicesIsEmpty() {
        DispatchingDeviceList deviceList = createDeviceList();

        assertThat(deviceList.getDevices()).isEmpty();
    }

    @Test
    void deviceIsCreatedForEachInitiallyConnectedDevice() {
        adbFacade.connectDevice(createDevice("DeviceA"));
        adbFacade.connectDevice(createDevice("DeviceB"));

        DispatchingDeviceList deviceList = createDeviceList();

        assertThat(deviceList.getAllDevices())
                .map(ProvisionalDevice::getSerialNumber)
                .as("Check serials of returned devices")
                .containsExactlyInAnyOrder("DeviceA", "DeviceB");
    }

    @Test
    void connectedDeviceIsInList() {
        DispatchingDeviceList deviceList = createDeviceList();

        adbFacade.connectDevice(createDevice("DeviceA"));

        assertThat(deviceList.getAllDevices())
                .map(ProvisionalDevice::getSerialNumber)
                .as("Check serial of returned device")
                .containsExactly("DeviceA");
    }

    @Test
    void connectedDeviceIsNotInListAfterDisconnect() {
        DispatchingDeviceList deviceList = createDeviceList();

        IDevice deviceA = adbFacade.connectDevice(createDevice("DeviceA"));
        adbFacade.disconnectDevice(deviceA);

        assertThat(deviceList.getDevices()).isEmpty();
    }

    @Test
    void notificationAboutConnectedDeviceIsPassedToRegisteredObserver() {
        DeviceChangeObserver observer = mock(DeviceChangeObserver.class);
        DispatchingDeviceList deviceList = createDeviceList();

        deviceList.addObserver(observer);
        adbFacade.connectDevice(createDevice("DeviceA"));

        verify(observer).onProvisionalDeviceConnected(argThat(hasSerial("DeviceA")));
        verify(observer).onDeviceConnected(argThat(hasSerial("DeviceA")));
    }

    @Test
    void disconnectingInitiallyConnectedDeviceTriggersNotification() {
        IDevice deviceA = adbFacade.connectDevice(createDevice("DeviceA"));
        adbFacade.connectDevice(createDevice("DeviceB"));
        DeviceChangeObserver observer = mock(DeviceChangeObserver.class);

        DispatchingDeviceList deviceList = createDeviceList();
        deviceList.addObserver(observer);

        adbFacade.disconnectDevice(deviceA);

        verify(observer, only()).onDeviceDisconnected(argThat(hasSerial("DeviceA")));
    }

    @Test
    void disconnectingConnectedDeviceTriggersNotification() {
        DeviceChangeObserver observer = mock(DeviceChangeObserver.class);

        DispatchingDeviceList deviceList = createDeviceList();
        IDevice deviceA = adbFacade.connectDevice(createDevice("DeviceA"));
        deviceList.addObserver(observer);

        adbFacade.disconnectDevice(deviceA);

        verify(observer, only()).onDeviceDisconnected(argThat(hasSerial("DeviceA")));
    }

    @ParameterizedTest
    @ValueSource(
            ints = {IDevice.CHANGE_STATE,
                    IDevice.CHANGE_BUILD_INFO,
                    IDevice.CHANGE_BUILD_INFO | IDevice.CHANGE_STATE,
                    IDevice.CHANGE_CLIENT_LIST | IDevice.CHANGE_STATE,
                    IDevice.CHANGE_CLIENT_LIST | IDevice.CHANGE_BUILD_INFO,
                    IDevice.CHANGE_BUILD_INFO | IDevice.CHANGE_CLIENT_LIST | IDevice.CHANGE_STATE})
    void deviceChangeNotifiesObservers(int changeMask) {
        IDevice deviceA = adbFacade.connectDevice(createDevice("DeviceA"));
        DeviceChangeObserver observer = mock(DeviceChangeObserver.class);

        DispatchingDeviceList deviceList = createDeviceList();
        deviceList.addObserver(observer);
        adbFacade.changeDevice(deviceA, changeMask);

        verify(observer, only()).onDeviceChanged(argThat(hasSerial("DeviceA")));
    }

    @Test
    void deviceClientListChangeDoesNotNotifyObservers() {
        IDevice deviceA = adbFacade.connectDevice(createDevice("DeviceA"));
        DeviceChangeObserver observer = mock(DeviceChangeObserver.class);

        DispatchingDeviceList deviceList = createDeviceList();
        deviceList.addObserver(observer);
        adbFacade.changeDevice(deviceA, IDevice.CHANGE_CLIENT_LIST);

        verify(observer, never()).onDeviceChanged(any());
    }

    @Test
    void outOfOrderDisconnectIsTolerated() {
        DeviceChangeObserver observer = mock(DeviceChangeObserver.class);
        DispatchingDeviceList deviceList = createDeviceList();
        deviceList.addObserver(observer);

        adbFacade.disconnectUnconnectedDevice(createDevice("DeviceA"));

        verifyNoInteractions(observer);
    }

    @Test
    void changeBeforeConnectIsTolerated() {
        DeviceChangeObserver observer = mock(DeviceChangeObserver.class);
        DispatchingDeviceList deviceList = createDeviceList();
        deviceList.addObserver(observer);

        adbFacade.changeNotConnectedDevice(createDevice("DeviceA"), IDevice.CHANGE_STATE);

        verify(observer).onProvisionalDeviceConnected(argThat(hasSerial("DeviceA")));
        verify(observer).onDeviceConnected(argThat(hasSerial("DeviceA")));
    }

    @Test
    void outOfOrderChangeAndConnectAreTolerated() {
        DeviceChangeObserver observer = mock(DeviceChangeObserver.class);
        DispatchingDeviceList deviceList = createDeviceList();
        deviceList.addObserver(observer);

        IDevice deviceA = createDevice("DeviceA");
        adbFacade.changeNotConnectedDevice(deviceA, IDevice.CHANGE_STATE);
        adbFacade.connectDevice(deviceA);

        InOrder order = inOrder(observer);
        order.verify(observer).onProvisionalDeviceConnected(argThat(hasSerial("DeviceA")));
        order.verify(observer).onDeviceConnected(argThat(hasSerial("DeviceA")));
        order.verify(observer).onDeviceChanged(argThat(hasSerial("DeviceA")));
        order.verifyNoMoreInteractions();
    }

    @Test
    void closingDeviceListUnsubscribesItFromUpdates() {
        var deviceList = createDeviceList();
        deviceList.close();

        assertThat(adbFacade.hasRegisteredListeners()).isFalse();
        assertThat(adbFacade.hasRegisteredBridgeObservers()).isFalse();
    }

    @Test
    void closingDeviceListNotifiesAboutDisconnectOfAllDevices() {
        adbFacade.connectDevice(createDevice("DeviceA"));
        var deviceList = createDeviceList();

        var observer = mock(DeviceChangeObserver.class);
        deviceList.addObserver(observer);
        deviceList.close();

        verify(observer).onDeviceDisconnected(argThat(hasSerial("DeviceA")));
    }

    @Test
    void provisionUpdatesDoNotTriggerNotificationsAfterClose() throws Exception {
        adbFacade.connectDevice(createDevice("DeviceA"));
        var provisioner = new FakeDeviceProvisioner();
        var deviceList = createDeviceList(provisioner);

        var observer = mock(DeviceChangeObserver.class);
        deviceList.addObserver(observer);
        deviceList.close();
        var inOrder = inOrder(observer);
        inOrder.verify(observer).onDeviceDisconnected(argThat(hasSerial("DeviceA")));

        provisioner.completeDeviceProvisioning("DeviceA");

        inOrder.verifyNoMoreInteractions();
    }

    private IDevice createDevice(String serial) {
        IDevice result = StrictMock.strictMock(IDevice.class);
        doReturn(serial).when(result).getSerialNumber();
        doReturn(true).when(result).isOnline();
        return result;
    }

    private DispatchingDeviceList createDeviceList() {
        return createDeviceList(createProvisioner());
    }

    private DispatchingDeviceList createDeviceList(DeviceProvisioner provisioner) {
        return DispatchingDeviceList.create(adbFacade, provisioner);
    }

    @SuppressWarnings("Convert2Lambda")
    private DeviceProvisioner createProvisioner() {
        return new DeviceProvisioner() {
            @Override
            public CompletableFuture<DeviceImpl> provisionDevice(ProvisionalDeviceImpl provisionalDevice) {
                return CompletableFuture.completedFuture(
                        new DeviceImpl(provisionalDevice.getDeviceKey(), provisionalDevice.getIDevice(),
                                new DeviceProperties(
                                        "product", "30", null, "fingerprint"
                                )));
            }
        };
    }
}

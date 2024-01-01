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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import name.mlopatkin.andlogview.base.concurrent.TestExecutor;
import name.mlopatkin.andlogview.base.concurrent.TestSequentialExecutor;
import name.mlopatkin.andlogview.test.StrictMock;

import com.android.ddmlib.IDevice;
import com.google.common.util.concurrent.MoreExecutors;

import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

class DispatchingDeviceListTest {
    private final FakeAdbFacade adbFacade = new FakeAdbFacade();

    private TestSequentialExecutor testExecutor;

    @BeforeEach
    void setUp() {
        testExecutor = new TestSequentialExecutor(MoreExecutors.directExecutor());
    }

    @Test
    void initialListOfDevicesIsEmpty() {
        var deviceList = createDeviceList();

        assertThat(deviceList.getDevices()).isEmpty();
    }

    @Test
    void deviceIsCreatedForEachInitiallyConnectedDevice() {
        adbFacade.connectDevice(createDevice("DeviceA"));
        adbFacade.connectDevice(createDevice("DeviceB"));

        var deviceList = createDeviceList();

        assertThatAllDevices(deviceList).containsExactlyInAnyOrder("DeviceA", "DeviceB");
    }

    @Test
    void connectedDeviceIsInList() {
        var deviceList = createDeviceList();

        adbFacade.connectDevice(createDevice("DeviceA"));

        assertThatAllDevices(deviceList).containsExactly("DeviceA");
    }

    @Test
    void connectedDeviceIsNotInListAfterDisconnect() {
        var deviceList = createDeviceList();

        IDevice deviceA = adbFacade.connectDevice(createDevice("DeviceA"));
        adbFacade.disconnectDevice(deviceA);

        assertThat(deviceList.getDevices()).isEmpty();
    }

    @Test
    void notificationAboutConnectedDeviceIsPassedToRegisteredObserver() {
        DeviceChangeObserver observer = mock();
        var deviceList = createDeviceList();

        deviceList.asObservable().addObserver(observer);
        adbFacade.connectDevice(createDevice("DeviceA"));

        verify(observer).onProvisionalDeviceConnected(argThat(hasSerial("DeviceA")));
        verify(observer).onDeviceConnected(argThat(hasSerial("DeviceA")));
    }

    @Test
    void disconnectingInitiallyConnectedDeviceTriggersNotification() {
        IDevice deviceA = adbFacade.connectDevice(createDevice("DeviceA"));
        adbFacade.connectDevice(createDevice("DeviceB"));
        DeviceChangeObserver observer = mock();

        var deviceList = createDeviceList();
        deviceList.asObservable().addObserver(observer);

        adbFacade.disconnectDevice(deviceA);

        verify(observer, only()).onDeviceDisconnected(argThat(hasSerial("DeviceA")));
    }

    @Test
    void disconnectingConnectedDeviceTriggersNotification() {
        DeviceChangeObserver observer = mock(DeviceChangeObserver.class);

        var deviceList = createDeviceList();
        IDevice deviceA = adbFacade.connectDevice(createDevice("DeviceA"));
        deviceList.asObservable().addObserver(observer);

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

        var deviceList = createDeviceList();
        deviceList.asObservable().addObserver(observer);
        adbFacade.changeDevice(deviceA, changeMask);

        verify(observer, only()).onDeviceChanged(argThat(hasSerial("DeviceA")));
    }

    @Test
    void deviceClientListChangeDoesNotNotifyObservers() {
        IDevice deviceA = adbFacade.connectDevice(createDevice("DeviceA"));
        DeviceChangeObserver observer = mock(DeviceChangeObserver.class);

        var deviceList = createDeviceList();
        deviceList.asObservable().addObserver(observer);
        adbFacade.changeDevice(deviceA, IDevice.CHANGE_CLIENT_LIST);

        verify(observer, never()).onDeviceChanged(any());
    }

    @Test
    void outOfOrderDisconnectIsTolerated() {
        DeviceChangeObserver observer = mock(DeviceChangeObserver.class);
        var deviceList = createDeviceList();
        deviceList.asObservable().addObserver(observer);

        adbFacade.disconnectUnconnectedDevice(createDevice("DeviceA"));

        verifyNoInteractions(observer);
    }

    @Test
    void changeBeforeConnectIsTolerated() {
        DeviceChangeObserver observer = mock(DeviceChangeObserver.class);
        var deviceList = createDeviceList();
        deviceList.asObservable().addObserver(observer);

        adbFacade.changeNotConnectedDevice(createDevice("DeviceA"), IDevice.CHANGE_STATE);

        verify(observer).onProvisionalDeviceConnected(argThat(hasSerial("DeviceA")));
        verify(observer).onDeviceConnected(argThat(hasSerial("DeviceA")));
    }

    @Test
    void outOfOrderChangeAndConnectAreTolerated() {
        DeviceChangeObserver observer = mock(DeviceChangeObserver.class);
        var deviceList = createDeviceList();
        deviceList.asObservable().addObserver(observer);

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
    }

    @Test
    void closingDeviceListNotifiesAboutDisconnectOfAllDevices() {
        adbFacade.connectDevice(createDevice("DeviceA"));
        var deviceList = createDeviceList();

        var observer = mock(DeviceChangeObserver.class);
        deviceList.asObservable().addObserver(observer);
        deviceList.close();

        verify(observer).onDeviceDisconnected(argThat(hasSerial("DeviceA")));
    }

    @Test
    void provisionUpdatesDoNotTriggerNotificationsAfterClose() throws Exception {
        adbFacade.connectDevice(createDevice("DeviceA"));
        var provisioner = new FakeDeviceProvisioner();
        var deviceList = createDeviceList(provisioner);

        var observer = mock(DeviceChangeObserver.class);
        deviceList.asObservable().addObserver(observer);
        deviceList.close();
        var inOrder = inOrder(observer);
        inOrder.verify(observer).onDeviceDisconnected(argThat(hasSerial("DeviceA")));

        provisioner.completeDeviceProvisioning("DeviceA");

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void devicesAddedInBackgroundAreNotVisibleInRunningListeners() throws Exception {
        var testExecutor = new TestExecutor();
        var deviceList = createDeviceList(testExecutor);

        var observer = mock(DeviceChangeObserver.class);
        doAnswer(device -> {
            ProvisionalDevice pd = device.getArgument(0);
            if ("deviceA".equals(pd.getSerialNumber())) {
                assertThatAllDevices(deviceList).containsExactlyInAnyOrder("deviceA");
            } else if ("deviceB".equals(pd.getSerialNumber())) {
                assertThatAllDevices(deviceList).containsExactlyInAnyOrder("deviceA", "deviceB");
            }
            return null;
        }).when(observer).onProvisionalDeviceConnected(any());

        deviceList.asObservable().addObserver(observer);

        adbFacade.connectDevice(createDevice("deviceA"));
        adbFacade.connectDevice(createDevice("deviceB"));

        testExecutor.flush();
    }

    @Test
    void pendingNotificationsAreNotDeliveredAfterClosingInCallback() {
        var testExecutor = new TestExecutor();
        var deviceList = createDeviceList(testExecutor);

        var observerA = mock(DeviceChangeObserver.class);

        var closingObserver = new DeviceChangeObserver() {
            @Override
            public void onProvisionalDeviceConnected(ProvisionalDevice device) {
                deviceList.close();
            }
        };

        deviceList.asObservable().addObserver(closingObserver);
        deviceList.asObservable().addObserver(observerA);

        adbFacade.connectDevice(createDevice("deviceA"));
        testExecutor.flush();

        verify(observerA, only()).onDeviceDisconnected(argThat(hasSerial("deviceA")));
    }

    private IDevice createDevice(String serial) {
        IDevice result = StrictMock.strictMock(IDevice.class);
        doReturn(serial).when(result).getSerialNumber();
        doReturn(true).when(result).isOnline();
        return result;
    }

    private AdbDeviceList createDeviceList() {
        return createDeviceList(createProvisioner());
    }

    private AdbDeviceList createDeviceList(DeviceProvisioner provisioner) {
        return DispatchingDeviceList.create(adbFacade, provisioner, testExecutor);
    }

    private AdbDeviceList createDeviceList(Executor executor) {
        return DispatchingDeviceList.create(adbFacade, createProvisioner(), new TestSequentialExecutor(executor));
    }

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

    private static AbstractListAssert<?, List<? extends String>, String, ObjectAssert<String>> assertThatAllDevices(
            AdbDeviceList deviceList) {
        return assertThat(deviceList.getAllDevices()).map(ProvisionalDevice::getSerialNumber);
    }
}

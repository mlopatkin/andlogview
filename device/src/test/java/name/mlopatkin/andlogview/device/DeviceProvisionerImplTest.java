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

import static com.google.common.util.concurrent.Futures.immediateFailedFuture;
import static com.google.common.util.concurrent.Futures.immediateFuture;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doReturn;

import name.mlopatkin.andlogview.test.StrictMock;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class DeviceProvisionerImplTest {
    private final FakeAdbFacade adbFacade = new FakeAdbFacade();

    @Test
    void offlineDeviceIsNotProvisionedImmediately() throws Exception {
        IDevice device = adbFacade.connectDevice(
                mockDevice("serial", false, immediateFuture("product"), immediateFuture("30"), immediateFuture(null),
                        immediateFuture("fingerprint")));

        DeviceProvisioner provisioner = new DeviceProvisionerImpl(adbFacade, MoreExecutors.directExecutor());
        CompletableFuture<AdbDeviceImpl> result =
                provisioner.provisionDevice(new ProvisionalAdbDeviceImpl(DeviceKey.of(device), device));

        assertThat(result).isNotDone();
    }

    @Test
    void offlineDeviceIsProvisionedWhenComesOnline() throws Exception {
        IDevice device = adbFacade.connectDevice(
                mockDevice("serial", false, immediateFuture("product"), immediateFuture("30"), immediateFuture(null),
                        immediateFuture("fingerprint")));

        DeviceProvisioner provisioner = new DeviceProvisionerImpl(adbFacade, MoreExecutors.directExecutor());
        CompletableFuture<AdbDeviceImpl> result =
                provisioner.provisionDevice(new ProvisionalAdbDeviceImpl(DeviceKey.of(device), device));
        setOnline(device);

        assertThat(result).isCompleted();
        assertThat(result.get().getProduct()).isEqualTo("product");
        assertThat(result.get().getApiString()).isEqualTo("30");
        assertThat(result.get().getBuildFingerprint()).isEqualTo("fingerprint");

        assertThat(adbFacade.hasRegisteredListeners()).isFalse();
    }

    @Test
    void onlineDeviceIsProvisionedImmediately() throws Exception {
        IDevice device = adbFacade.connectDevice(
                mockDevice("serial", true, immediateFuture("product"), immediateFuture("30"), immediateFuture(null),
                        immediateFuture("fingerprint")));

        DeviceProvisioner provisioner = new DeviceProvisionerImpl(adbFacade, MoreExecutors.directExecutor());
        CompletableFuture<AdbDeviceImpl> result =
                provisioner.provisionDevice(new ProvisionalAdbDeviceImpl(DeviceKey.of(device), device));

        assertThat(result).isCompleted();
        assertThat(result.get().getProduct()).isEqualTo("product");
        assertThat(result.get().getApiString()).isEqualTo("30");
        assertThat(result.get().getBuildFingerprint()).isEqualTo("fingerprint");

        assertThat(adbFacade.hasRegisteredListeners()).isFalse();
    }

    @Test
    void deviceProvisioningIsBlockedOnPropertyRetrieval() throws Exception {
        SettableFuture<String> productFuture = SettableFuture.create();
        IDevice device = adbFacade.connectDevice(
                mockDevice("serial", true, productFuture, immediateFuture("30"), immediateFuture(null),
                        immediateFuture("fingerprint")));
        ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

        DeviceProvisioner provisioner = new DeviceProvisionerImpl(adbFacade, backgroundExecutor);
        CompletableFuture<AdbDeviceImpl> result =
                provisioner.provisionDevice(new ProvisionalAdbDeviceImpl(DeviceKey.of(device), device));

        assertThat(result).isNotDone();

        productFuture.set("product");

        CountDownLatch waitForCompletion = new CountDownLatch(1);
        backgroundExecutor.execute(waitForCompletion::countDown);
        waitForCompletion.await();

        assertThat(backgroundExecutor.shutdownNow()).as("There should be no pending work").isEmpty();
        assertThat(backgroundExecutor.isTerminated()).as("Background work should be completed at this point").isTrue();

        assertThat(result).isCompleted();
        assertThat(result.get().getProduct()).isEqualTo("product");
        assertThat(result.get().getApiString()).isEqualTo("30");
        assertThat(result.get().getBuildFingerprint()).isEqualTo("fingerprint");

        assertThat(adbFacade.hasRegisteredListeners()).isFalse();
    }

    @Test
    void deviceProvisioningFailsIfOfflineDeviceDisconnects() {
        IDevice device = adbFacade.connectDevice(
                mockDevice("serial", false, immediateFuture("product"), immediateFuture("30"), immediateFuture(null),
                        immediateFuture("fingerprint")));

        DeviceProvisioner provisioner = new DeviceProvisionerImpl(adbFacade, MoreExecutors.directExecutor());
        CompletableFuture<AdbDeviceImpl> result =
                provisioner.provisionDevice(new ProvisionalAdbDeviceImpl(DeviceKey.of(device), device));

        adbFacade.disconnectDevice(device);

        assertThat(result).isCompletedExceptionally();

        assertThat(adbFacade.hasRegisteredListeners()).isFalse();
    }

    @Test
    void deviceProvisioningFailsIfPropertyRetrievalFails() {
        IDevice device = adbFacade.connectDevice(
                mockDevice("serial", true, immediateFailedFuture(new TimeoutException("Failed to retrieve product")),
                        immediateFuture("30"), immediateFuture(null), immediateFuture("fingerprint")));

        DeviceProvisioner provisioner = new DeviceProvisionerImpl(adbFacade, MoreExecutors.directExecutor());
        CompletableFuture<AdbDeviceImpl> result =
                provisioner.provisionDevice(new ProvisionalAdbDeviceImpl(DeviceKey.of(device), device));

        assertThat(result).isCompletedExceptionally();

        assertThat(adbFacade.hasRegisteredListeners()).isFalse();
    }

    @Test
    void deviceProvisioningNotProceedIfOtherDeviceComesOnline() {
        IDevice device = adbFacade.connectDevice(
                mockDevice("serial", false, immediateFuture("product"), immediateFuture("30"), immediateFuture(null),
                        immediateFuture("fingerprint")));
        IDevice otherDevice = adbFacade.connectDevice(mockDevice("other", false,
                immediateFuture("product"),
                immediateFuture("30"),
                immediateFuture(null),
                immediateFuture("fingerprint")));

        DeviceProvisioner provisioner = new DeviceProvisionerImpl(adbFacade, MoreExecutors.directExecutor());
        CompletableFuture<AdbDeviceImpl> result =
                provisioner.provisionDevice(new ProvisionalAdbDeviceImpl(DeviceKey.of(device), device));
        setOnline(otherDevice);

        assertThat(result).isNotDone();
    }

    @Test
    void deviceProvisioningNotProceedIfDeviceChangesStateButIsNotOnline() {
        IDevice device = adbFacade.connectDevice(
                mockDevice("serial", false, immediateFuture("product"), immediateFuture("30"), immediateFuture(null),
                        immediateFuture("fingerprint")));

        DeviceProvisioner provisioner = new DeviceProvisionerImpl(adbFacade, MoreExecutors.directExecutor());
        CompletableFuture<AdbDeviceImpl> result =
                provisioner.provisionDevice(new ProvisionalAdbDeviceImpl(DeviceKey.of(device), device));
        adbFacade.changeDevice(device, IDevice.CHANGE_STATE);

        assertThat(result).isNotDone();
    }

    private IDevice mockDevice(String serial, boolean isOnline, Future<String> deviceProduct,
            Future<String> apiLevel,
            Future<String> codename, Future<String> fingerprint) {
        IDevice result = StrictMock.strictMock(IDevice.class);
        doReturn(serial).when(result).getSerialNumber();
        doReturn(isOnline).when(result).isOnline();
        doReturn(deviceProduct).when(result).getSystemProperty(DeviceProperties.PROP_BUILD_PRODUCT);
        doReturn(apiLevel).when(result).getSystemProperty(DeviceProperties.PROP_BUILD_API_LEVEL);
        doReturn(codename).when(result).getSystemProperty(DeviceProperties.PROP_BUILD_CODENAME);
        doReturn(fingerprint).when(result).getSystemProperty(DeviceProperties.PROP_BUILD_FINGERPRINT);
        return result;
    }

    private void setOnline(IDevice device) {
        doReturn(true).when(device).isOnline();
        adbFacade.changeDevice(device, IDevice.CHANGE_STATE);
    }
}

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

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.Client;
import com.android.ddmlib.FileListingService;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.ScreenRecorderOptions;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.SyncService;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.log.LogReceiver;
import com.android.sdklib.AndroidVersion;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Helper similar to the FilterInputStream that makes overriding tons of methods of the IDevice easier.
 */
class DelegatingDevice implements IDevice {
    private final IDevice inner;

    public DelegatingDevice(IDevice inner) {
        this.inner = inner;
    }

    @Override
    public String getSerialNumber() {
        return inner.getSerialNumber();
    }

    @Override
    public String getAvdName() {
        return inner.getAvdName();
    }

    @Override
    public DeviceState getState() {
        return inner.getState();
    }

    @Override
    @Deprecated
    public Map<String, String> getProperties() {
        return inner.getProperties();
    }

    @Override
    @Deprecated
    public int getPropertyCount() {
        return inner.getPropertyCount();
    }

    @Override
    public String getProperty(String name) {
        return inner.getProperty(name);
    }

    @Override
    public boolean arePropertiesSet() {
        return inner.arePropertiesSet();
    }

    @Override
    @Deprecated
    public String getPropertySync(String name)
            throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        return inner.getPropertySync(name);
    }

    @Override
    @Deprecated
    public String getPropertyCacheOrSync(String name)
            throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        return inner.getPropertyCacheOrSync(name);
    }

    @Override
    public boolean supportsFeature(Feature feature) {
        return inner.supportsFeature(feature);
    }

    @Override
    public boolean supportsFeature(HardwareFeature feature) {
        return inner.supportsFeature(feature);
    }

    @Override
    public String getMountPoint(String name) {
        return inner.getMountPoint(name);
    }

    @Override
    public boolean isOnline() {
        return inner.isOnline();
    }

    @Override
    public boolean isEmulator() {
        return inner.isEmulator();
    }

    @Override
    public boolean isOffline() {
        return inner.isOffline();
    }

    @Override
    public boolean isBootLoader() {
        return inner.isBootLoader();
    }

    @Override
    public boolean hasClients() {
        return inner.hasClients();
    }

    @Override
    public Client[] getClients() {
        return inner.getClients();
    }

    @Override
    public Client getClient(String applicationName) {
        return inner.getClient(applicationName);
    }

    @Override
    public SyncService getSyncService() throws TimeoutException, AdbCommandRejectedException, IOException {
        return inner.getSyncService();
    }

    @Override
    public FileListingService getFileListingService() {
        return inner.getFileListingService();
    }

    @Override
    public RawImage getScreenshot() throws TimeoutException, AdbCommandRejectedException, IOException {
        return inner.getScreenshot();
    }

    @Override
    public RawImage getScreenshot(long timeout, TimeUnit unit)
            throws TimeoutException, AdbCommandRejectedException, IOException {
        return inner.getScreenshot(timeout, unit);
    }

    @Override
    public void startScreenRecorder(String remoteFilePath, ScreenRecorderOptions options,
            IShellOutputReceiver receiver)
            throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
        inner.startScreenRecorder(remoteFilePath, options, receiver);
    }

    @Override
    @Deprecated
    public void executeShellCommand(String command, IShellOutputReceiver receiver,
            int maxTimeToOutputResponse)
            throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        inner.executeShellCommand(command, receiver, maxTimeToOutputResponse);
    }

    @Override
    public void executeShellCommand(String command, IShellOutputReceiver receiver)
            throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        inner.executeShellCommand(command, receiver);
    }

    @Override
    public void runEventLogService(LogReceiver receiver)
            throws TimeoutException, AdbCommandRejectedException, IOException {
        inner.runEventLogService(receiver);
    }

    @Override
    public void runLogService(String logname, LogReceiver receiver)
            throws TimeoutException, AdbCommandRejectedException, IOException {
        inner.runLogService(logname, receiver);
    }

    @Override
    public void createForward(int localPort, int remotePort)
            throws TimeoutException, AdbCommandRejectedException, IOException {
        inner.createForward(localPort, remotePort);
    }

    @Override
    public void createForward(int localPort, String remoteSocketName,
            DeviceUnixSocketNamespace namespace) throws TimeoutException, AdbCommandRejectedException, IOException {
        inner.createForward(localPort, remoteSocketName, namespace);
    }

    @Override
    public void removeForward(int localPort, int remotePort)
            throws TimeoutException, AdbCommandRejectedException, IOException {
        inner.removeForward(localPort, remotePort);
    }

    @Override
    public void removeForward(int localPort, String remoteSocketName,
            DeviceUnixSocketNamespace namespace) throws TimeoutException, AdbCommandRejectedException, IOException {
        inner.removeForward(localPort, remoteSocketName, namespace);
    }

    @Override
    public String getClientName(int pid) {
        return inner.getClientName(pid);
    }

    @Override
    public void pushFile(String local, String remote)
            throws IOException, AdbCommandRejectedException, TimeoutException, SyncException {
        inner.pushFile(local, remote);
    }

    @Override
    public void pullFile(String remote, String local)
            throws IOException, AdbCommandRejectedException, TimeoutException, SyncException {
        inner.pullFile(remote, local);
    }

    @Override
    public void installPackage(String packageFilePath, boolean reinstall, String... extraArgs) throws InstallException {
        inner.installPackage(packageFilePath, reinstall, extraArgs);
    }

    @Override
    public void installPackages(List<File> apks, boolean reinstall,
            List<String> installOptions, long timeout, TimeUnit timeoutUnit) throws InstallException {
        inner.installPackages(apks, reinstall, installOptions, timeout, timeoutUnit);
    }

    @Override
    public String syncPackageToDevice(String localFilePath)
            throws TimeoutException, AdbCommandRejectedException, IOException, SyncException {
        return inner.syncPackageToDevice(localFilePath);
    }

    @Override
    public void installRemotePackage(String remoteFilePath, boolean reinstall, String... extraArgs)
            throws InstallException {
        inner.installRemotePackage(remoteFilePath, reinstall, extraArgs);
    }

    @Override
    public void removeRemotePackage(String remoteFilePath) throws InstallException {
        inner.removeRemotePackage(remoteFilePath);
    }

    @Override
    public String uninstallPackage(String packageName) throws InstallException {
        return inner.uninstallPackage(packageName);
    }

    @Override
    public void reboot(String into) throws TimeoutException, AdbCommandRejectedException, IOException {
        inner.reboot(into);
    }

    @Override
    public boolean root()
            throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
        return inner.root();
    }

    @Override
    public boolean isRoot()
            throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
        return inner.isRoot();
    }

    @Override
    @Deprecated
    public Integer getBatteryLevel()
            throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
        return inner.getBatteryLevel();
    }

    @Override
    @Deprecated
    public Integer getBatteryLevel(long freshnessMs)
            throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
        return inner.getBatteryLevel(freshnessMs);
    }

    @Override
    public Future<Integer> getBattery() {
        return inner.getBattery();
    }

    @Override
    public Future<Integer> getBattery(long freshnessTime, TimeUnit timeUnit) {
        return inner.getBattery(freshnessTime, timeUnit);
    }

    @Override
    public List<String> getAbis() {
        return inner.getAbis();
    }

    @Override
    public int getDensity() {
        return inner.getDensity();
    }

    @Override
    public String getLanguage() {
        return inner.getLanguage();
    }

    @Override
    public String getRegion() {
        return inner.getRegion();
    }

    @Override
    public AndroidVersion getVersion() {
        return inner.getVersion();
    }

    @Override
    public String getName() {
        return inner.getName();
    }

    @Override
    public void executeShellCommand(String command, IShellOutputReceiver receiver,
            long maxTimeToOutputResponse, TimeUnit maxTimeUnits)
            throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        inner.executeShellCommand(command, receiver, maxTimeToOutputResponse, maxTimeUnits);
    }

    @Override
    public void executeShellCommand(String command, IShellOutputReceiver receiver, long maxTimeout,
            long maxTimeToOutputResponse, TimeUnit maxTimeUnits)
            throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        inner.executeShellCommand(command, receiver, maxTimeout, maxTimeToOutputResponse, maxTimeUnits);
    }

    @Override
    public Future<String> getSystemProperty(String name) {
        return inner.getSystemProperty(name);
    }
}

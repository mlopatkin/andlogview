---
title: Working with a device or an emulator
weight: 20
---
AndLogView can display logs directly from an emulator or a device if you have
the Android SDK installed. Simply run a launching script and the AndLogView
will try to connect to the first available device.
If there is no available devices the tool will be waiting for the device to
connect.

You can use "ADB > Connect to device" in the main menu to select a device
to connect to.

![Device selection window](select_device.png)

"ADB > Reset logs" command or <kbd><kbd>Ctrl</kbd><kbd>R</kbd></kbd> clears already retrieved logs,
bookmarks and processes list.

## ADB Configuration

"ADB > Configuration..." command opens the configuration window. You should
enter the path to the adb executable if it is not on the PATH.

![Configuration dialog](configuration.png)

If the "Reconnect to device automatically" checkbox is set, then AndLogView will
automatically reconnect to the available device if the one currently connected
to disconnects. Use this with caution, because already collected logs are lost
upon reconnecting.

## Installing the Android SDK

You don't need a full Android SDK to capture logs from a device,
just the `adb` from SDK platform-tools and, on Windows, an appropriate device
driver. If you're running an emulator, chances are you already have a full
Android SDK installed.

### Automated Installation

AndLogView can download and install the platform tools for you. In the ADB
Configuration dialog, click the "Install ADB" button. This will guide you
through:

1. Reviewing and accepting the Android SDK license
2. Selecting a directory where platform-tools will be installed
3. Downloading and unpacking the tools

Once complete, AndLogView will automatically configure itself to use the
installed ADB.

{{< hint type=note >}}
The automated installer downloads platform-tools directly from Google's Android
SDK repository. An internet connection is required.
{{< /hint >}}

### Manual Installation

Alternatively, you can download the platform tools for your operating system
manually from
[developer.android.com](https://developer.android.com/tools/releases/platform-tools#downloads)
and configure the path in the ADB Configuration dialog.

## Connecting to device over USB

You need to [set up the device and your machine](https://developer.android.com/studio/run/device#setting-up)
to be able to connect to the device over USB. Below you can find a quick recap
of necessary steps.

{{< tabs "system_configuration" >}}
{{< tab "Windows" >}}
### Installing device drivers on Windows

Device drivers for Google devices (Pixel and Nexus lines) can be downloaded from
the [developer.android.com](https://developer.android.com/studio/run/win-usb).

{{< hint type=note >}}
You don't need to use SDK Manager if you don't have the Android Studio or
the full Android SDK installed, use the
"Download the Google USB Driver ZIP file (ZIP)" link.
{{< /hint >}}

Device drivers for devices from other manufacturers are distributed separately.
Some links and the instruction of how to install the driver are also available
on the [developer.android.com](https://developer.android.com/studio/run/oem-usb).
{{< /tab >}}

{{< tab "Linux" >}}
### Configuring permission on Linux
No extra drivers are necessary for Linux, but permissions may need to be
adjusted.

You can follow the instruction on
[developer.android.com](https://developer.android.com/studio/run/device#setting-up)
to do that.
{{< /tab >}}

{{< tab "macOS" >}}
macOS needs no extra configuration.
{{< /tab >}}

{{< /tabs >}}

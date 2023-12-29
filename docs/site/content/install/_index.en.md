---
title: Installation
weight: 20
---

## Requirements
* Windows, Linux, or MacOS X
* JRE or JDK 8+
* Android SDK (optional, to work with a device or an emulator)

The Java executable (`javaw.exe` on Windows and `java` on Linux/MacOS X) is
expected to be on `PATH`. Alternatively, the `JAVA_HOME` environment variable
should point to the JRE/JDK.

## Installing

Download the [latest release archive](https://github.com/mlopatkin/andlogview/releases/latest) from the GitHub, and unpack it into the location of your
choice.

To run the logview, use the OS-specific script:
* `bin/logview.bat` (Windows)
* `bin/logview` (Linux/MacOS X)

<!--
TODO(mlopatkin): https://github.com/mlopatkin/andlogview/issues/339
If adb.exe/adb aren't on the `PATH` you should manually set its location at the
first launch. You will be prompted about it:

![ADB setup prompt](adb_setup_prompt.png)

Click "Yes" to open [[AdbMode | ADB setup dialog]]. However it is necessary
for working with device/emulator only.
-->

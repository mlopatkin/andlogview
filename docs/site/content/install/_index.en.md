---
title: Installing
weight: 10
---

## Requirements
* Windows, Linux, or MacOS X
* Java: JRE or JDK 8+ (version 17 is recommended)
* Android SDK Platform-Tools (optional, to work with a device or an emulator)

The Java executable (`javaw.exe` on Windows and `java` on Linux/MacOS X) is
expected to be on `PATH`. Alternatively, the `JAVA_HOME` environment variable
should point to the JRE/JDK.

### Installing Java

If you don't have Java installed, you can download
[Eclipse Temurin JRE](https://adoptium.net/temurin/releases/?version=17&os=any&package=jre)
for your operating system and hardware architecture.

## Installing

Download the [latest release archive][gh_latest_release] from the GitHub, and
unpack it into the location of your choice.

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

[gh_latest_release]: https://github.com/mlopatkin/andlogview/releases/latest
[temurin_download]: https://adoptium.net/temurin/releases/?version=17

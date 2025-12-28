## What's New

This is a bugfix release for release 1.0.

### Fixed issues of 1.0

* Incorrect version information (`-SNAPSHOT`) ([#501](https://github.com/mlopatkin/andlogview/issues/501)).

### Breaking Changes

* **Minimal supported JDK to run AndLogView is now 17**.

  Starting with AndLogView version 1.0, JDK 17 or higher is required to run it. Before, only Java 8 was required.
  You can use new distributions with bundled JDK if you don't want to manage JDK yourself.

### New Features
* **Semi-automated ADB installation** ([#239](https://github.com/mlopatkin/andlogview/issues/239)).
  AndLogView can now download and install Android SDK platform-tools for you.

  This makes it easier to start working with devices and emulators without manually downloading the SDK.

* **Platform-specific installers with bundled Java runtime** ([#428](https://github.com/mlopatkin/andlogview/issues/428)).

  There is no need to install JDK manually if you use one of these. So far, these platforms are supported:
    * Linux (x64 aka amd64), only for DEB-based distributions, e.g. Debian, Ubuntu, Mint ([#437](https://github.com/mlopatkin/andlogview/issues/437)).
    * Windows (x64), as EXE installer ([#436](https://github.com/mlopatkin/andlogview/issues/436)).
    * macOS (arm64 aka M1+), as DMG image ([#438](https://github.com/mlopatkin/andlogview/issues/438)).

  Users on other platforms should continue use the `noJRE` distribution with manually installed JDK 17+ for now.

### Improvements
* **App Icon**.

  The app now has a custom icon.

* **About dialog** ([#426](https://github.com/mlopatkin/andlogview/issues/426)).

  A new Help → About menu item shows application version, build information, and open-source component licenses.

* **Better error reporting**.

  Error dialogs now include expandable stack traces to help diagnose issues and provide better context when things go wrong.

### More info:
* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.23...1.0.1)
* [Known issues](https://github.com/mlopatkin/andlogview/issues?q=is%3Aissue+label%3Aaffects-version%3A1.0.1+-milestone%3A1.0.1+). Closed issues in this list are fixed in the later version.

## What's New

### New Features
* Semi-automated ADB installation ([#239](https://github.com/mlopatkin/andlogview/issues/239)).
  AndLogView can now download and install Android SDK platform-tools for you.

  This makes it easier to start working with devices and emulators without manually downloading the SDK.
* Platform-specific installers with bundled Java runtime ([#428](https://github.com/mlopatkin/andlogview/issues/428)).
  There is no need to install JDK manually if you use one of these. So far, these platforms are supported:
  * Linux (x64 aka amd64), only for DEB-based distributions, e.g. Debian, Ubuntu, Mint ([#437](https://github.com/mlopatkin/andlogview/issues/437)).
  * Windows (x64), as EXE installer ([#436](https://github.com/mlopatkin/andlogview/issues/436)).
  * macOS (arm64 aka M1+), as DMG image ([#438](https://github.com/mlopatkin/andlogview/issues/438)).

  Users on other platforms should continue use the `noJRE` distribution with manually installed JDK for now.
* The app now has a custom icon.

### More info:
* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.23...master)
* [Known issues](https://github.com/mlopatkin/andlogview/issues?q=sort%3Aupdated-desc%20is%3Aissue%20label%3Aa%3Abug%2Ca%3Aregression%20label%3Aaffects-version%3A0.24%20is%3Aopen)

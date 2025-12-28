# AndLogView version history

## 1.0.1
This is a bugfix release for release 1.0.

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/1.0...1.0.1)
* [Known issues](https://github.com/mlopatkin/andlogview/issues?q=is%3Aissue+label%3Aaffects-version%3A1.0.1)

### 🛠 Improvements and bugfixes

* Incorrect version information (`-SNAPSHOT`) ([#501](https://github.com/mlopatkin/andlogview/issues/501)).

## 1.0

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.23...1.0)
* [Known issues](https://github.com/mlopatkin/andlogview/issues?q=is%3Aissue+label%3Aaffects-version%3A1.0+-milestone%3A1.0+)

### 💥 Breaking Changes

* Minimal supported JDK to run AndLogView is now 17

### 🆕 New Features
* Semi-automated ADB installation ([#239](https://github.com/mlopatkin/andlogview/issues/239)).
* Platform-specific installers with bundled Java runtime ([#428](https://github.com/mlopatkin/andlogview/issues/428)).

### 🛠 Improvements and bugfixes
* New app Icon.
* About dialog ([#426](https://github.com/mlopatkin/andlogview/issues/426)).
* Better error reporting.

## 0.23

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.22...0.23)
* [Known issues](https://github.com/mlopatkin/andlogview/issues?q=is%3Aissue+label%3Aaffects-version%3A0.23)

### 🆕 New Features
* Filter panel with button replaced with filter list with checkboxes ([#162](https://github.com/mlopatkin/andlogview/issues/162)).
* Filters can be named, names appear in UI ([#126](https://github.com/mlopatkin/andlogview/issues/126))
* Index window only apply show and hide filters that are above their filter in the list ([#164](https://github.com/mlopatkin/andlogview/issues/164)).
* Filters can be reordered. This affects index and highlight filters ([#164](https://github.com/mlopatkin/andlogview/issues/164)).
* Index windows have titles based on filter names or criteria ([#146](https://github.com/mlopatkin/andlogview/issues/146)).
* Modernized Look&Feel on macOS ([#220](https://github.com/mlopatkin/andlogview/issues/220), [#403](https://github.com/mlopatkin/andlogview/issues/403)).

### 🛠 Improvements and bugfixes
* The outdated Log4J 1.2 dependency is no longer used. The security risk was low, but security scanners may have alerted because of its presence ([#422](https://github.com/mlopatkin/andlogview/issues/422)).
* “ADB failed to start” dialog on startup is now suppressible ([#339](https://github.com/mlopatkin/andlogview/issues/339)).

## 0.22

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.21.1...0.22)
* [Known issues](https://github.com/mlopatkin/andlogview/issues?q=is%3Aissue+label%3Aaffects-version%3A0.22)

### 🆕 New Features
* Compatibility with Android 14 ([#327](https://github.com/mlopatkin/andlogview/issues/327))
* No restart required to change the ADB configuration ([#197](https://github.com/mlopatkin/andlogview/issues/197), [#326](https://github.com/mlopatkin/andlogview/issues/326))
* Support for the “long” format ([#169](https://github.com/mlopatkin/andlogview/issues/169))
* More reliable parsing of the multiline log messages (stacktraces) ([#169](https://github.com/mlopatkin/andlogview/issues/169))
* Improved extraction of process information for dumpstates ([#336](https://github.com/mlopatkin/andlogview/issues/336), [#277](https://github.com/mlopatkin/andlogview/issues/277), [#88](https://github.com/mlopatkin/andlogview/issues/88), [#119](https://github.com/mlopatkin/andlogview/issues/119), [#85](https://github.com/mlopatkin/andlogview/issues/85))

### 🛠 Improvements and bugfixes
* “Disconnected” status now properly shown in the status bar ([#330](https://github.com/mlopatkin/andlogview/issues/330))
* "Delete" key now deletes bookmarks properly ([#149](https://github.com/mlopatkin/andlogview/issues/149))
* Proper file path copied when using status bar menu ([#350](https://github.com/mlopatkin/andlogview/issues/350))
* Fixed crash because of the missing preference directory on the very first run ([#317](https://github.com/mlopatkin/andlogview/issues/317))

### Contributors
Thanks to [@nevack](https://github.com/nevack) for their contributions.

## 0.21.1

This is a bugfix release for the release 0.21.

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.21...0.21.1)

### 🛠 Improvements and bugfixes

* Fixed misaligned filter buttons ([#218](https://github.com/mlopatkin/andlogview/issues/218))

## 0.21

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.20...0.21)

### 💥 Breaking Changes

* The app is now named AndLogView to avoid using trademarked names.
* The development has moved from Bitbucket to Github.

### 🆕 New Features
* Better HiDPI support on Windows and Linux on Java 8 and higher, including fractional scaling ([#136](https://github.com/mlopatkin/andlogview/issues/136))
* Modernized look&feel on Windows and Linux ([#136](https://github.com/mlopatkin/andlogview/issues/136))
* Better MacOS support: platform-native shortcuts, global menu bar support, and native look&feel
* Improved compatibility with Android P+ ([#167](https://github.com/mlopatkin/andlogview/issues/167), [#213](https://github.com/mlopatkin/andlogview/issues/213), [#223](https://github.com/mlopatkin/andlogview/issues/223))
* The logs from the Android Studio's logcat view are now supported ([#159](https://github.com/mlopatkin/andlogview/issues/159))
* Device serial or file path can be copied to clipboard with the context menu of the status panel (bottom right corner) ([#137](https://github.com/mlopatkin/andlogview/issues/137))
* A device dump with various logs can be produced if the app runs in debug mode (with `-d` switch). The dump aims to help improving compatibility

### 🛠 Improvements and bugfixes
* The AndLogView's build is now deterministic, i. e. you can produce byte-to-byte identical binary ([#157](https://github.com/mlopatkin/andlogview/issues/157))
* Esc key closes dialogs (filter editor, device selector, and configuration) ([#108](https://github.com/mlopatkin/andlogview/issues/108))
* “Device disconnected” alert only shows when viewing the logs from a device ([#96](https://github.com/mlopatkin/andlogview/issues/96))
* Configuration dialog highlights invalid ADB location if one is entered
* Fixed the “ability” to open the filter editor twice for the same filter ([#155](https://github.com/mlopatkin/andlogview/issues/155))

### Contributors
Thanks to [@nevack](https://github.com/nevack) and [Sunny An](https://bitbucket.org/sunnyan_kr/) for their contributions.

## 0.20

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.19...0.20)

### 🆕 New Features
* Quick filters based on the content of the clicked cell.
* Allow to use comma in pattern in filter dialog for tags/pids/apps (either by doubling a comma or by enclosing the
  whole pattern in backticks).

### 💥 Known Issues

* It isn't possible to use backticks to wrap slash-enclosed patterns.
In the next versions it will be allowed to force plain text search even if the pattern looks like regex (e. g. `/Foo/`).

## 0.19

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.18.2...0.19)

### 💥 Breaking Changes

* The app now requires Java 8 to run.

### 🆕 New Features
* Filters are saved between app runs.
* Column order can be customized.
* Columns can be hidden.
* Empty columns (for which there is no data in the source) aren't displayed.
* Tag filter understands regexps.

### 🛠 Improvements and bugfixes
* The app can be run with Java 9+.
* The app is now compatible with Android M-O.

## 0.18.2

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.18.1...0.18.2)

### 🆕 New Features

* Support for Android 5.0, including new separate `crash` log

## 0.18.1

This is a bugfix release.

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.18...0.18.1)

### 🛠 Improvements and bugfixes
* Recompiled with Java 6 compatibility.
* Fixed NPE when saving configuration.
* Improved Linux startup script.

## 0.18

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.17.4...0.18)

### 🆕 New Features
* Application name is now displayed as column.
* It is possible to search and filter by application name.
* Search scope can be limited to application name, tag or message by prefixing request.
* Window size and position is persisted between runs.
* Log viewer reconnects to the device automatically if enabled in settings.

### 🛠 Improvements and bugfixes
* Improved compatibility with various log format deviations.

## 0.17.4

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.17.3...0.17.4)

### 🛠 Improvements and bugfixes
* Added `Ctrl+R` shortcut to reset the logs.

## 0.17.3

This is a bugfix release.

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.17.2...0.17.3)

### 🛠 Improvements and bugfixes
* Fixed classpath issue if the path to the bat file contains spaces.
* Fixed exception when viewing logs after the device was disconnected.
* Fixed app hanging if the emulator becomes unresponsive.
* Fixed app hanging when connecting to the running emulator.
* Fixed incorrect log output for DDMS errors and warnings.
* Started emulator is now added to the “Select device” dialog correctly.

## 0.17.2

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.17.1...0.17.2)

### 🆕 New Features
* Logs are now saved in native ADB format.

### 🛠 Improvements and bugfixes
* Ordering of the log lines was reworked to be as natural as possible.
* Fixed crash when opening ill-formatted logfile.
* Added notification for the user on uncaught exceptions.

## 0.17.1

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.17...0.17.1)

### 🛠 Improvements and bugfixes
* Scrolling if there are many filters.
* Dialogs are shown over the main window after `Alt-Tab`.
* Filtering process list from logging-related stuff.

## 0.17

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.16...0.17)

### 🆕 New Features
* Regular expression search and filter.
* List of processes.
* Copy lines with a context menu.

### 🛠 Improvements and bugfixes
* Tooltips for truncated cells.
* Matches are highlighted in tooltips too.
* Multiple UI glitches fixed.

## 0.16

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.15...0.16)

### 💥 Breaking Changes

* Distribution form changed: now distributed as a ZIP archive with a bat file to run.

### 🆕 New Features
* Notification about current connection or opened file.
* Menu command to show bookmarks window.
* The tool can start ADB server if it is not running.
* ADB library from the SDK can be connected instead of custom one.
* Properties are now stored in `%APPDATA%\logview\logview.properties` on Windows or `~/.logview/logview.properties` on Linux/macOS

### 🛠 Improvements and bugfixes
* Improved responsiveness during the startup.
* Open and save dialogs remember last used directory.
* Bookmarks are cleared after reset if it makes sense (in the ADB mode).

## 0.15

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.14...0.15)

### 🆕 New Features
* Saving logs.
* Keyboard shortcuts for main menu commands.
* Highlight bookmarked records.
* Copy text from PID, tag or message.
* Reading logs and dumpstates in older Android formats.

### 🛠 Improvements and bugfixes
* Fixed crash when opening a dumpstate file specified from the command-line
* “Pin” was renamed to “Bookmark”
* Available buffers on the device are detected
* Minor bugfixes and refactorings

## 0.14

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.13...0.14)

### 🆕 New Features
* Open a file in main menu.
* Select a device to get logs from.
* Drag&drop support for files.
* Multiple colors for highlighting filters.

### 🛠 Improvements and bugfixes
* “Reset logs” menu item moved into main menu.
* Buffer type selection moved into main menu.
* Introduced a status bar.
* Notification about the current device disconnecting.

## 0.13

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.12...0.13)

### 🆕 New Features
* Reset logs: the option to drop logs collected so far in the ADB mode.

## 0.12

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.11...0.12)

### 🆕 New Features
* Case-insensitive search.
* Show records matching a filter in a separate, so called “index” window.

## 0.11

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.10...0.11)

### 🆕 New Features
* Parse simple log files in `threadtime` and `brief` formats.

### 🛠 Improvements and bugfixes
* Show a message if the search text is not found.

## 0.10

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.9.1...0.10)

### 🆕 New Features
* Filter by the type of logs: `main`, `events`, `radio`, etc.
* Use DDMLIB to interact with ADB.

## 0.9.1

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.9...0.9.1)

### 🛠 Improvements and bugfixes
* Fixed copying log records.

## 0.9

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.8...0.9)

### 🆕 New Features
* Toggling filter effects to temporarily disable.

### 🛠 Improvements and bugfixes
* Search hotkey changed to `Ctrl+F`.

## 0.8

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.7...0.8)

### 🆕 New Features
* Text search backwards.
* Search results highlighting.
* Pinning records; a separate window to see pinned records.

## 0.7

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.6...0.7)

### 🆕 New Features
* Text search across log records.

## 0.6

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.5...0.6)

### 🆕 New Features
* Show app names for PIDs when opening dumpstate files.

## 0.5

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.4...0.5)

### 🆕 New Features
* Edit filters.

## 0.4

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.3...0.4)

### 🆕 New Features
* Supports both “show only matching” and “hide matching” filters.

## 0.3

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.2...0.3)

### 🆕 New Features
* Parse dumpstate files.

## 0.2

* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/0.1...0.2)

### 🆕 New Features
* Filtering by tags, PIDs, log priority, and message text.
* Highlight filtered records.
* Show only records matching the filter.

## 0.1

Initial release

* [Full Changelog](https://github.com/mlopatkin/andlogview/commits/0.1)

### 🆕 New Features
* Display ADB output from files and live.
* Priority-based coloring.
* Autoscroll.

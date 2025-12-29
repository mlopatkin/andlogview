# AndLogView version history
<#--
Macro: A single release. For best formatting, use:

## <@release name="X.YY" previousRelease="X.ZZ">
<#t>
... Release contents ...
</@release>

hasIssues parameter controls if a link to `affects-version:X.YY` query is generated.
-->
<#macro release name previousRelease hasIssues=true>
Version [${name}](https://github.com/mlopatkin/andlogview/releases/tag/${name})
<#-- The comment line visually separates releases to make the resulting MD readable -->
<!----------------------------------------------------------------------------->

<#if previousRelease?has_content>
  * [Full Changelog](https://github.com/mlopatkin/andlogview/compare/${previousRelease}...${name})
<#else>
  * [Full Changelog](https://github.com/mlopatkin/andlogview/commits/${name})
</#if>
<#if hasIssues>
  * [Known issues](https://github.com/mlopatkin/andlogview/issues?q=is%3Aissue+label%3Aaffects-version%3A${name})
</#if>
<#nested/>
</#macro>


<#macro GH issueNumber>[#${issueNumber}](https://github.com/mlopatkin/andlogview/issues/${issueNumber})</#macro>

## <@release name="1.0.1" previousRelease="1.0">
<#t>
### 🛠 Improvements and bugfixes
* Incorrect version information (`-SNAPSHOT`) (<@GH 501/>).
</@release>


## <@release name="1.0" previousRelease="0.23">
<#t>
### 💥 Breaking Changes
* Minimal supported JDK to run AndLogView is now 17

### 🆕 New Features
* Semi-automated ADB installation (<@GH 239/>).
* Platform-specific installers with bundled Java runtime (<@GH 428/>).

### 🛠 Improvements and bugfixes
* New app Icon.
* About dialog (<@GH 426/>).
* Better error reporting.
</@release>


## <@release name="0.23" previousRelease="0.22">
<#t>
### 🆕 New Features
* Filter panel with button replaced with filter list with checkboxes (<@GH 162/>).
* Filters can be named, names appear in UI (<@GH 126/>)
* Index window only apply show and hide filters that are above their filter in the list (<@GH 164/>).
* Filters can be reordered. This affects index and highlight filters (<@GH 164/>).
* Index windows have titles based on filter names or criteria (<@GH 146/>).
* Modernized Look&Feel on macOS (<@GH 220/>, <@GH 403/>).

### 🛠 Improvements and bugfixes
* The outdated Log4J 1.2 dependency is no longer used (<@GH 422/>).
  The security risk was low, but security scanners may have alerted because of its presence (<@GH 422/>).
* “ADB failed to start” dialog on startup is now suppressible (<@GH 339/>).
</@release>


## <@release name="0.22" previousRelease="0.21.1">
<#t>
### 🆕 New Features
* Compatibility with Android 14 (<@GH 327/>)
* No restart required to change the ADB configuration (<@GH 197/>, <@GH 326/>)
* Support for the “long” format (<@GH 169/>)
* More reliable parsing of the multiline log messages (stacktraces) (<@GH 169/>)
* Improved extraction of process information for dumpstates (<@GH 336/>, <@GH 277/>, <@GH 88/>,
  <@GH 119/>, <@GH 85/>)

### 🛠 Improvements and bugfixes
* “Disconnected” status now properly shown in the status bar (<@GH 330/>)
* "Delete" key now deletes bookmarks properly (<@GH 149/>)
* Proper file path copied when using status bar menu (<@GH 350/>)
* Fixed crash because of the missing preference directory on the very first run (<@GH 317/>)

### 🤝 Contributors
Thanks to [@nevack](https://github.com/nevack) for their contributions.
</@release>


## <@release name="0.21.1" previousRelease="0.21" hasIssues=false>
<#t>
### 🛠 Improvements and bugfixes
* Fixed misaligned filter buttons (<@GH 218/>)
</@release>


## <@release name="0.21" previousRelease="0.20">
<#t>
### 💥 Breaking Changes
* The app is now named AndLogView to avoid using trademarked names (<@GH 158/>).
* The development has moved from Bitbucket to Github.

### 🆕 New Features
* Better HiDPI support on Windows and Linux on Java 8 and higher, including fractional scaling (<@GH 136/>)
* Modernized look&feel on Windows and Linux (<@GH 136/>)
* Better MacOS support: platform-native shortcuts, global menu bar support, and native look&feel
* Improved compatibility with Android P+ (<@GH 167/>, <@GH 213/>, <@GH 223/>)
* The logs from the Android Studio's logcat view are now supported (<@GH 159/>)
* Device serial or file path can be copied to clipboard with the context menu of the status panel (bottom right corner)
  (<@GH 137/>)
* A device dump with various logs can be produced if the app runs in debug mode (with `-d` switch).
  The dump aims to help improving compatibility.

### 🛠 Improvements and bugfixes
* The AndLogView's build is now deterministic, i. e. you can produce byte-to-byte identical binary (<@GH 157/>)
* Esc key closes dialogs (filter editor, device selector, and configuration) (<@GH 108/>)
* “Device disconnected” alert only shows when viewing the logs from a device (<@GH 96/>)
* Configuration dialog highlights invalid ADB location if one is entered
* Fixed the “ability” to open the filter editor twice for the same filter (<@GH 155/>)

### 🤝 Contributors
Thanks to [@nevack](https://github.com/nevack) and [Sunny An](https://bitbucket.org/sunnyan_kr/) for their
contributions.
</@release>


## <@release name="0.20" previousRelease="0.19">
<#t>
### 🆕 New Features
* Quick filters based on the content of the clicked cell (<@GH 93/>).
* Allow to use comma in pattern in filter dialog for tags/pids/apps (either by doubling a comma or by enclosing the
  whole pattern in backticks) (<@GH 154/>, (<@GH 153/>)).

### 🛠 Improvements and bugfixes
* Editing a filter no longer makes it active (<@GH 123/>).
* Fixed a crash when deleting a filter while editing it (<@GH 121/>).

### 💥 Known Issues
* It isn't possible to use backticks to wrap slash-enclosed patterns.
In the next versions it will be allowed to force plain text search even if the pattern looks like regex (e. g. `/Foo/`).
</@release>


## <@release name="0.19" previousRelease="0.18.2">
<#t>
### 💥 Breaking Changes
* The app now requires Java 8 to run.

### 🆕 New Features
* Filters are saved between app runs (<@GH 89/>).
* Column order can be customized (<@GH 72/>).
* Columns can be hidden (<@GH 72/>).
* Empty columns (for which there is no data in the source) aren't displayed (<@GH 73/>).
* Tag filter understands regexps (<@GH 111/>).

### 🛠 Improvements and bugfixes
* The app can be run with Java 9+ (<@GH 132/>).
* More log formats now supported (<@GH 120/>, <@GH 127/>, <@GH 129/>).
* The app is now compatible with Android M-O (<@GH 114/>, <@GH 133/>, <@GH 128/>).
</@release>


## <@release name="0.18.2" previousRelease="0.18.1" hasIssues=false>
<#t>
### 🆕 New Features
* Support for Android 5.0, including new separate `crash` log (<@GH 109/>, <@GH 110/>).
</@release>


## <@release name="0.18.1" previousRelease="0.18">
<#t>
### 🛠 Improvements and bugfixes
* Recompiled with Java 6 compatibility (<@GH 99/>).
* Fixed NPE when saving configuration (<@GH 100/>).
* Improved Linux startup script (<@GH 101/>).
</@release>


## <@release name="0.18" previousRelease="0.17.4">
<#t>
### 🆕 New Features
* Application name is now displayed as column (<@GH 71/>).
* It is possible to search and filter by application name (<@GH 70/>).
* Search scope can be limited to application name, tag or message by prefixing request (<@GH 69/>).
* Window size and position is persisted between runs (<@GH 82/>).
* Log viewer reconnects to the device automatically if enabled in settings (<@GH 95/>).

### 🛠 Improvements and bugfixes
* Improved compatibility with various log format deviations (<@GH 79/>, <@GH 92/>, <@GH 90/>).
</@release>


## <@release name="0.17.4" previousRelease="0.17.3">
<#t>
### 🛠 Improvements and bugfixes
* Added `Ctrl+R` shortcut to reset the logs (<@GH 78/>).
</@release>


## <@release name="0.17.3" previousRelease="0.17.2">
<#t>
### 🛠 Improvements and bugfixes
* Fixed classpath issue if the path to the bat file contains spaces (<@GH 65/>).
* Fixed exception when viewing logs after the device was disconnected (<@GH 68/>).
* Fixed app hanging if the emulator becomes unresponsive (<@GH 67/>).
* Fixed app hanging when connecting to the running emulator (<@GH 74/>).
* Fixed incorrect log output for DDMS errors and warnings (<@GH 66/>).
* Started emulator is now added to the “Select device” dialog correctly (<@GH 64/>).
  </@release>


## <@release name="0.17.2" previousRelease="0.17.1">
<#t>
### 🆕 New Features
* Logs are now saved in native ADB format (<@GH 60/>).

### 🛠 Improvements and bugfixes
* Ordering of the log lines was reworked to be as natural as possible (<@GH 61/>, <@GH 62/>).
* Fixed crash when opening ill-formatted logfile (<@GH 59/>).
* Added notification for the user on uncaught exceptions.
</@release>


## <@release name="0.17.1" previousRelease="0.17">
<#t>
### 🛠 Improvements and bugfixes
* Scrolling if there are many filters (<@GH 57/>).
* Dialogs are shown over the main window after `Alt-Tab` (<@GH 58/>).
* Filtering process list from logging-related stuff.
</@release>


## <@release name="0.17" previousRelease="0.16">
<#t>
### 🆕 New Features
* Regular expression search and filter (<@GH 50/>).
* List of processes (<@GH 51/>).
* Copy lines with a context menu (<@GH 47/>).

### 🛠 Improvements and bugfixes
* Tooltips for truncated cells (<@GH 38/>).
* Matches are highlighted in tooltips too (<@GH 56/>).
* Multiple UI glitches fixed.
</@release>


## <@release name="0.16" previousRelease="0.15.1">
<#t>
### 💥 Breaking Changes
* Distribution form changed: now distributed as a ZIP archive with a bat file to run.

### 🆕 New Features
* Status bar tells about current connection or opened file (<@GH 40/>).
* Menu command to show bookmarks window (<@GH 26/>).
* The tool can start ADB server if it is not running (<@GH 39/>).
* ADB library from the SDK can be connected instead of custom one (<@GH 37/>).
* Properties are now stored in `%APPDATA%\logview\logview.properties` on Windows or `~/.logview/logview.properties` on
  Linux/macOS.

### 🛠 Improvements and bugfixes
* Improved responsiveness during startup (<@GH 179/>).
* Open and save dialogs remember last used directory (<@GH 35/>).
* Bookmarks are cleared after reset if it makes sense (in the ADB mode) (<@GH 41/>).
</@release>


## <@release name="0.15.1" previousRelease="0.15">
<#t>
### 🛠 Improvements and bugfixes
* Fixed handling of offline devices (<@GH 17/>, <@GH 36/>).
</@release>


## <@release name="0.15" previousRelease="0.14.1">
<#t>
### 🆕 New Features
* Saving logs (<@GH 19/>).
* Keyboard shortcuts for main menu commands (<@GH 25/>).
* Highlight bookmarked records (<@GH 27/>).
* Copy text from PID, tag or message (<@GH 24/>).
* Reading logs and dumpstates in older Android formats (<@GH 31/>).

### 🛠 Improvements and bugfixes
* Fixed crash when opening a dumpstate file specified from the command-line (<@GH 30/>).
* “Pin” was renamed to “Bookmark” (<@GH 22/>).
* Available buffers on the device are detected (<@GH 32/>).
* Minor bugfixes and refactorings.
</@release>


## <@release name="0.14.1" previousRelease="0.14">
<#t>
### 🛠 Improvements and bugfixes
* Can select a device to connect by double click in the dialog (<@GH 21/>).
* Fixed crash when copying log records in the `brief` format (<@GH 18/>).
* Fixed connected device taking over an opened file (<@GH 15/>).
</@release>


## <@release name="0.14" previousRelease="0.13">
<#t>
### 🆕 New Features
* Open a file in main menu (<@GH 7/>).
* Select a device to get logs from (<@GH 8/>).
* Drag&drop support for files (<@GH 5/>).
* Multiple colors for highlighting filters (<@GH 12/>).

### 🛠 Improvements and bugfixes
* “Reset logs” menu item moved into main menu.
* Buffer type selection moved into main menu.
* Introduced a status bar (<@GH 9/>).
* Notification about the current device disconnecting (<@GH 6/>).
</@release>


## <@release name="0.13" previousRelease="0.12">
<#t>
### 🆕 New Features
* Reset logs: the option to drop logs collected so far in the ADB mode.
</@release>


## <@release name="0.12" previousRelease="0.11" hasIssues=false>
<#t>
### 🆕 New Features
* Case-insensitive search.
* Show records matching a filter in a separate, so called “index” window.
</@release>


## <@release name="0.11" previousRelease="0.10">
<#t>
### 🆕 New Features
* Parse simple log files in `threadtime` and `brief` formats.

### 🛠 Improvements and bugfixes
* Show a message if the search text is not found.
</@release>


## <@release name="0.10" previousRelease="0.9.1">
<#t>
### 🆕 New Features
* Filter by the type of logs: `main`, `events`, `radio`, etc.
* Use DDMLIB to interact with ADB.
</@release>


## <@release name="0.9.1" previousRelease="0.9">
<#t>
### 🛠 Improvements and bugfixes
* Fixed copying log records (<@GH 176/>).
</@release>


## <@release name="0.9" previousRelease="0.8.1">
<#t>
### 🆕 New Features
* Toggling filter effects to temporarily disable.

### 🛠 Improvements and bugfixes
* Search hotkey changed to `Ctrl+F`.
</@release>


## <@release name="0.8.1" previousRelease="0.8" hasIssues=false>
<#t>
### 🛠 Improvements and bugfixes
* Fix crash when highlighting search results.
</@release>


## <@release name="0.8" previousRelease="0.7.1" hasIssues=false>
<#t>
### 🆕 New Features
* Text search backwards.
* Search results highlighting.
* Pinning records; a separate window to see pinned records.
</@release>


## <@release name="0.7.1" previousRelease="0.7" hasIssues=false>
<#t>
### 🛠 Improvements and bugfixes
* Use the currently selected row as the search starting point.
</@release>


## <@release name="0.7" previousRelease="0.6" hasIssues=false>
<#t>
### 🆕 New Features
* Text search across log records.
</@release>


## <@release name="0.6" previousRelease="0.5" hasIssues=false>
<#t>
### 🆕 New Features
* Show app names for PIDs when opening dumpstate files.
</@release>


## <@release name="0.5" previousRelease="0.4.2" hasIssues=false>
<#t>
### 🆕 New Features
* Edit filters.
</@release>


## <@release name="0.4.2" previousRelease="0.4.1" hasIssues=false>
<#t>
### 🛠 Improvements and bugfixes
* Fixed configuration file lookup.
</@release>


## <@release name="0.4.1" previousRelease="0.4" hasIssues=false>
<#t>
### 🛠 Improvements and bugfixes
* Fixed icons not being loaded from a standalone JAR.
</@release>


## <@release name="0.4" previousRelease="0.3" hasIssues=false>
<#t>
### 🆕 New Features
* Supports both “show only matching” and “hide matching” filters.
</@release>


## <@release name="0.3" previousRelease="0.2" hasIssues=false>
<#t>
### 🆕 New Features
* Parse dumpstate files.
</@release>


## <@release name="0.2" previousRelease="0.1" hasIssues=false>
<#t>
### 🆕 New Features
* Filtering by tags, PIDs, log priority, and message text.
* Highlight filtered records.
* Show only records matching the filter.
</@release>


## <@release name="0.1" previousRelease="" hasIssues=false>
Initial release

### 🆕 New Features
* Display ADB output from files and live.
* Priority-based coloring.
* Autoscroll.
</@release>

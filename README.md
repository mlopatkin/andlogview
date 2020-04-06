# AndLogView: Log viewer for Android development

https://bitbucket.org/mlopatkin/android-log-viewer

Android log viewer is the tool to simplify analysis of the Android logs.

![Application window](https://bitbucket.org/mlopatkin/android-log-viewer/wiki/multiple_filters.png)

## Features

- Display logs from a device or an emulator
- Display saved logs and dumpstate files
- Search in the logs using regex if needed
- Bookmarks
- Filter log lines by tag, PID, app name, log priority, message content
- Filtered lines can be hidden, highlighted or shown in a separate window
- Display any avaliable buffers: main, system, events, radio
- Save filtered lines
- Display process list

## Installing

Requirements:

 - JRE 8+
 - Android SDK (to work with a device or an emulator)

Unpack a zip archive to any folder.

Use the OS-specific script to launch AndLogView:

 - `bin/andlogview.bat` (Windows)
 - `bin/andlogview` (Linux)

Both scripts expect the javaw/java to be on your PATH.

## Overview: filters

The following filtering criteria are supported:

- Tag(s)
- Message text
- PID(s) and/or application name(s)
- Log priority

Filtered lines can be:

- Showed (all other lines will be hidden)
- Hidden
- Highlighted
- Showed in a separate window

You may enter several tags, application names or PIDs separated with commas, any
of them will be filtered. Message, tag and application name filters support
regular expressions. If several criteria are specified, only records that
match all of them will be filtered. If the tag or application name contain `,`
then `,` symbol should be doubled or the whole pattern should be enclosed in
backticks. For example, if you want to search for tags `Foo,bar` and `Bar,baz`
you should enter `Foo,,bar, Bar,,baz` or `` `Foo,bar`, `Bar,baz` ``. Wrapping
in backticks doesn't work for regular expressions.

## Overview: searching

1. `Ctrl+F` - show search field
2. Enter the pattern, then press `Enter` to start searching
3. `F3` - find next, `Shift+F3` - find previous
4. `Esc` - clear search results

You should wrap your pattern into `/.../` - slashes - to search using regular
expressions, ex. `/^[Aa].*$/` - all lines that start with lower or upper 'a'.
Regex-based searching is case-sensitive in opposite to a simple searching.
Standard Java regular expression syntax is used.

Searching is performed in application name, tag and message fields. You can
limit search scope to the single field by prefixing search request with `app:`,
`tag:`, or `msg:` respectively. Only one (first) prefix is in effect, others are
treated like normal search pattern.

## Overview: other

- The bookmarks windows can be used for quick jumping between marked lines.
  Use context menu to add a line to bookmarks.
- You can copy log lines to the clipboard using `Ctrl+C`, `Ctrl+Ins` or the
  context menu.
- Double-click on tag, message or pid cell opens edit mode where you can
  select and copy a substring of the cell's content

See the complete manual at https://bitbucket.org/mlopatkin/android-log-viewer/wiki

## Contacts

There is a mailing list [android-log-viewer at google groups](https://groups.google.com/forum/#!forum/android-log-viewer)
for release announcements and discussions.

Please report bugs to https://bitbucket.org/mlopatkin/android-log-viewer/issues

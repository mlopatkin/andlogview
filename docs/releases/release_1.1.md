No new features announced yet.

### 💥 Breaking Changes
* Customizing the app through `logview.properties` is no longer
  supported ([#510](https://github.com/mlopatkin/andlogview/issues/510)).

  Preferences already existing in this file are imported on the first run of
  1.1, but further changes are ignored.
* Old mode with filter buttons on the bottom has been completely removed
  ([#405](https://github.com/mlopatkin/andlogview/issues/405)).

  It has been replaced by the new filter list that provides a cleaner view to
  the list of available filters.

### 🛠 Improvements and bugfixes
* Fixed issue when the last message in the buffer may not be displayed until the
  next message comes ([#513](https://github.com/mlopatkin/andlogview/issues/513)).
* Converted changelog to markdown format and added more historical contents
  ([#232](https://github.com/mlopatkin/andlogview/issues/232)).

### More info:
* [Full Changelog](https://github.com/mlopatkin/andlogview/compare/1.0.1...master)
* [Known issues](https://github.com/mlopatkin/andlogview/issues?q=is%3Aissue%20is%3Aopen%20(label%3Aa%3Abug%20OR%20label%3Aa%3Aregression))

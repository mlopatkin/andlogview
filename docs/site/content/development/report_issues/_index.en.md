---
title: Report an issue
---

The project uses [GitHub issue tracker](https://github.com/mlopatkin/andlogview/issues/).
Please search for the existing issue before submitting a new one.

## How to request a new feature
[Create a new issue](https://github.com/mlopatkin/andlogview/issues/new) with
the title briefly describing new feature. The more elaborate description goes
into "Description" field.

A project member will look at it.

## How to report a bug
[Create a new issue](https://github.com/mlopatkin/andlogview/issues/new) with
the title briefly describing the bug. Make sure to the include following
information in bug description:
* JVM version (usually "java -version")
* Occurence rate (always, sometimes, once)
* Steps to reproduce the bug
* Expected and actual results of these steps
* If bug is related to ADB usage: SDK version and device name
* If bug is related to a log file/dumpstate file: attach the file to an issue

It would be nice if you attach a log file of the app, which is located in
the temporary files directory (`C:\Users\<username>\AppData\Local\Temp`
on Windows and `/tmp` on Linux) and named `logview.log`.

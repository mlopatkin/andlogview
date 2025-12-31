# Contributing to AndLogView

## Reporting issues

The project uses [GitHub issue tracker](https://github.com/mlopatkin/andlogview/issues/).
Please search for the existing issue before submitting a new one.

### How to request a new feature
[Create a new issue](https://github.com/mlopatkin/andlogview/issues/new) with
the title briefly describing new feature. The more elaborate description goes
into "Description" field.

A project member will look at it.

### How to report a bug
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

## Submitting Pull Requests

### Finding an issue to work on

The project uses two labels to highlight issues recommended for external
contribution:
- [`good first issue`](https://github.com/mlopatkin/andlogview/issues?q=state%3Aopen+label%3A%22good+first+issue%22)
  &mdash; issues good for newcomers, not yet familiar with the project.
- [`help wanted`](https://github.com/mlopatkin/andlogview/issues?q=state%3Aopen%20label%3A%22help%20wanted%22)
  &mdash; issues where outside expertise is especially appreciated, but not
  necessarily easy to tackle. They may require some prior knowledge.

### Building and testing

See [`BUILDING.md`](BUILDING.md) for recommendations on how to set up the
environment, check out and build the sources, run tests.

### Submitting a PR

There are some mandatory steps to follow when creating a PR:

* [Write good commit messages.](https://cbea.ms/git-commit/#seven-rules)
* [Sign off your commits](https://git-scm.com/docs/git-commit#Documentation/git-commit.txt---signoff) to indicate that
  you agree to the terms of [Developer Certificate of Origin](https://developercertificate.org/).
  We can only accept PRs that have all commits signed off.
* [Sign your commits](https://docs.github.com/en/authentication/managing-commit-signature-verification/signing-commits)
  with a PGP/SSH key. This is not the same as signing off.

Link each commits to the issue by appending a line:
```
Issue: #<issue number>
```
For example, `Issue: #510`. Commits that do not belong to a particular issue may
use `Issue: n/a`.

When creating a PR, ensure that “Allow edits from maintainers” is on, so we can
address minor things without bothering you.

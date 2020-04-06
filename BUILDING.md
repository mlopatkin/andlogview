# BUILDING ANDLOGVIEW

## Prerequisites

 * Windows or Linux
 * OpenJDK 8 (e. g. from [AdoptOpenJDK](https://adoptopenjdk.net/))
 * Internet connection to download dependencies from jcenter, Maven Central, etc.
 * Git (to check out and commit)

## Checking out the sources
The repository can be cloned with Git or you can download a latest snapshot in archive from [Downloads][downloads] page
on Bitbucket. The rest of this chapter assumes that you use Git.

This project uses Unix-style line endings (LF) for all files on all platforms except for Windows Batch (bat/cmd) files
that must use CRLF. I personally prefer to disable line ending translations in Git and configure editors to use
Unix-style line endings. It can be disabled globally, for all Git repos on the machine (beware, it may cause strange
side-effects for existing checkouts):
```
git config --global core.autocrlf false
```
It is possible to configure it only for this repository later after checkout.

To get sources run `git clone` as usual:
```
git clone https://mlopatkin@bitbucket.org/mlopatkin/android-log-viewer.git
```

If you decided to disable line ending translation only for this repo then run following command inside repo:
```bash
git config core.autocrlf false
# Delete every tracked file in the repo (your uncommited local changes will be lost)
git ls-files -z | xargs -0 rm -f
# Restore tracked files
git checkout -f .
```

Another useful thing is the option to ignore some mechanical commits (reformats) from `git blame`. To do this you need a
recent version of Git and specify file with a list of ignored revisions:
```
git config blame.ignoreRevsFile .git-blame-ignore-revs
```

## Set up hooks

There is a linting pre-commit hook that can be installed for convenience. It checks that files are well-formed before
commit (the same check also runs in pull-requests): proper line endings are used, there are no trailing whitespaces,
each line ends with line ending symbol, there are no tab symbols. To install the hook copy `tools/hooks/pre-commit` to
`.git/hooks/` directory. Make sure that the copy is marked executable.

## Building with Gradle
Run `./gradlew assemble` to build everything. First run takes some time because Gradle binaries and app dependencies
must be downloaded from repositories.

Run `./gradlew check` to run tests.

Run `./gradlew shadowDistZip` to prepare distributive package. The output is placed into `build/distributions`.
You can specify JDK to use with JAVA_HOME environment variable or by setting `org.gradle.java.home=path/to/jdk/8` in
`gradle.settings` file in the root directory of the project.

## Importing into IDE
#### IDEA
You can use [IDEA Community Edition][idea] 14 or later. I didn't check earlier versions. Use "File > New Project From
Existing Sources... > (Select project directory) > Gradle". Do not use "Create separate module per source set".
Annotation processing should work "out-of-the-box".

For newer versions (I've tested 2019.3.4) use "File > New Project From Existing Sources... > (Select build.gradle in
project directory)" or use "Import Existing Project..." in the startup wizard.

There are project-specific codestyle settings that can be imported. Open "File > Settings > Editor > Code Style". Select
"Project" for "Scheme", then click on Gear icon, select "Import scheme...". Select
`config/idea/idea-codestyle-for-import.xml` in the project directory. Mark "Current scheme" checkbox. The settings will
be imported for this project only. You can also import copyright profiles from `config/idea/copyright` or just copy this
directory into `.idea` (but not when IDEA is running).

### Eclipse
Import project into your Eclipse workspace with "File > Import... > Gradle > Existing Gradle Project". Annotation
processing should work "out-of-the-box". Eclipse is still a preferred way to work with GUI because of the WindowBuilder
plugin.

[downloads]: https://bitbucket.org/mlopatkin/android-log-viewer/downloads/
[idea]: https://www.jetbrains.com/idea/

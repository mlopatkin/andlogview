# BUILDING LOGVIEWER

## Prerequisites

 * Windows or Linux
 * OpenJDK 7 or Oracle JDK 7
 * Internet connection to download dependencies from jcenter, Maven Central, etc.

While the project can be built with Java 8, it still targets Java 7.

## Building with Gradle
Run `./gradlew assemble` to build everything. First run takes some time because Gradle binaries and app dependencies
must be downloaded from repositories.

Run `./gradlew check` to run tests.

Run `./gradlew distShadowZip` to prepare distributive package. The output is placed into `build/distributions`.

## Building with Ant (deprecated)
You'll need [Apache Ant (1.9+)][ant] with [Apache Ivy (2.4.0+)][ivy] plugin installed. Run `ant test` to build and execute tests, and
`ant dist` to prepare distributive package. Use `ant -p` to get list of all available tasks.

## Importing into IDE
#### IDEA
You can use [IDEA Community Edition][idea] 14 or later. I didn't check earlier versions. Use "File > New Project From Existing
Sources... > (Select project folder) > Gradle"".

This project uses annotation processors to generate sources. IDEA cannot import this from Gradle script so this must be
configured manually:
 1. Go to "File > Project Structure > Libraries", click on '+' and then choose "From Maven...". Specify
    "com.google.dagger:dagger-compiler:2.0", click on "Search" button, then click "OK".
 2. Go to "File > Settings > Build, Execution, Deployment > Compiler > Annotation Processors". Select "Enable annotation
    processing" and "Obtain processors from project classpath". You can use 'gen/' and 'gen_tests/' as output folders.
 3. Run "Build".
 4. Mark 'gen/' as a "Generated source root" in context menu.
 5. Run "Buid > Rebuild"

Please note that "Sync with gradle" will unmark 'gen/' and compilation in IDEA will break. You need to re-mark the
folder again after each sync.

### Eclipse (deprecated)
Run `./gradlew eclipse`. Then import project into your Eclipse workspace. Annotation processing should work
"out-of-the-box".

[ant]: https://ant.apache.org/
[ivy]: https://ant.apache.org/ivy/download.cgi
[idea]: https://www.jetbrains.com/idea/
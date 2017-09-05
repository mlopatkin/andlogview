# BUILDING LOGVIEWER

## Prerequisites

 * Windows or Linux
 * OpenJDK 8 or Oracle JDK 8
 * Internet connection to download dependencies from jcenter, Maven Central, etc.

## Building with Gradle
Run `./gradlew assemble` to build everything. First run takes some time because Gradle binaries and app dependencies
must be downloaded from repositories.

Run `./gradlew check` to run tests.

Run `./gradlew distShadowZip` to prepare distributive package. The output is placed into `build/distributions`.
You can specify JDK to use with JAVA_HOME environment variable or by setting `org.gradle.java.home=path/to/jdk/8` in
`gradle.settings` file in the root directory of the project.

## Importing into IDE
#### IDEA
You can use [IDEA Community Edition][idea] 14 or later. I didn't check earlier versions. Use "File > New Project From Existing
Sources... > (Select project folder) > Gradle"".

This project uses annotation processors to generate sources. IDEA cannot import processor settings from Gradle script so this
must be configured manually:
 1. Go to "File > Project Structure > Libraries", click on '+' and then choose "From Maven...". Specify
    "com.google.dagger:dagger-compiler:2.8" (check for actual version in build.gradle), click on "Search" button, then
    click  "OK".
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

[idea]: https://www.jetbrains.com/idea/

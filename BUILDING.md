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
Sources... > (Select project folder) > Gradle"". Do not use "Create separate module per source set". Annotation
processing should work "out-of-the-box".

### Eclipse
Run `./gradlew eclipse`. Then import project into your Eclipse workspace. Annotation processing should work
"out-of-the-box". Eclipse is still a prefered way to work with GUI because of the WindowBuilder plugin.

[idea]: https://www.jetbrains.com/idea/

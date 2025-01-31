/*
 * Copyright 2015 Mikhail Lopatkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import name.mlopatkin.andlogview.building.GenerateNotices
import name.mlopatkin.andlogview.building.buildLibs
import name.mlopatkin.andlogview.building.disableTasks
import name.mlopatkin.andlogview.building.theBuildDir
import name.mlopatkin.bitbucket.gradle.UploadTask

plugins {
    application

    alias(libs.plugins.shadow)
    alias(libs.plugins.bitbucket)
    alias(libs.plugins.jmh)

    id("name.mlopatkin.andlogview.building.build-environment")
    id("name.mlopatkin.andlogview.building.installers")
    id("name.mlopatkin.andlogview.building.java-conventions")
    id("name.mlopatkin.andlogview.building.metadata")
}

dependencies {
    implementation(project(":base"))
    implementation(project(":device"))
    implementation(project(":filters"))
    implementation(project(":logmodel"))
    implementation(project(":parsers"))
    implementation(project(":search"))
    implementation(project(":search:logrecord"))
    implementation(project(":widgets"))

    implementation(libs.dagger.runtime)
    implementation(libs.flatlaf.core)
    implementation(libs.flatlaf.extras)
    implementation(libs.gson)
    implementation(libs.jopt)
    implementation(libs.slf4j.api)
    implementation(libs.log4j)
    implementation(libs.miglayout)

    runtimeOnly(libs.slf4j.reload4j)

    annotationProcessor(buildLibs.dagger.compiler)

    testImplementation(testFixtures(project(":base")))
    testImplementation(testFixtures(project(":filters")))
    testImplementation(testFixtures(project(":logmodel")))
    testImplementation(testFixtures(project(":search")))

    jmhImplementation(testFixtures(project(":parsers")))
}

configurations {
    runtimeClasspath {
        // These annotations aren't needed in runtime.
        exclude(group = "com.google.code.findbugs", module = "jsr305")
        exclude(group = "org.checkerframework", module = "checker-qual")
        exclude(group = "org.jspecify", module = "jspecify")
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        exclude(group = "com.google.j2objc", module = "j2objc-annotations")
        exclude(group = "org.codehaus.mojo", module = "animal-sniffer-annotations")
        // Main Guava artifact includes listenable future, so the stub module isn't needed.
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
}

version = libs.versions.andlogview.get() + (if (buildEnvironment.isSnapshot) "-SNAPSHOT" else "")

application {
    applicationName = "andlogview"
    mainClass = "name.mlopatkin.andlogview.Main"
}

jmh {
    humanOutputFile = theBuildDir.file("reports/jmh/human.txt")
}

sourceSets {
    main {
        java {
            srcDirs.clear()
            srcDir("src")
        }
        resources {
            srcDirs.clear()
            srcDir("resources")
            srcDir("third-party/fontawesomeIcons/resources")
            srcDir("third-party/tangoIcons/resources")
            srcDir("third-party/themes/resources")
        }
    }
    test {
        java {
            srcDirs.clear()
            srcDir("test")
        }
        resources {
            srcDirs.clear()
            srcDir("test_resources")
        }
    }
    this.jmh {
        java {
            srcDirs.clear()
            srcDir("jmh/java")
        }
        resources {
            srcDirs.clear()
            srcDir("jmh/resources")
            srcDir("test_resources")
        }
    }
}

tasks.named<ProcessResources>(sourceSets.main.get().processResourcesTaskName) {
    // There is no better way of including a subset of files from a directory as resources.
    from(file("assets")) {
        include("andlogview.svg")
        into("name/mlopatkin/andlogview/")
    }
}

// Configure build metadata generator
buildMetadata {
    revision = buildEnvironment.sourceRevision
    packageName = "name.mlopatkin.andlogview"
    version = provider { project.version.toString() }
}

val generateNotices = tasks.register<GenerateNotices>("generateNotices") {
    bundledDependencies = configurations.runtimeClasspath.flatMap { rtCp ->
        rtCp.incoming.artifacts.resolvedArtifacts.map { artifacts ->
            artifacts.map { artifact -> artifact.id.componentIdentifier }
                .filterIsInstance<ModuleComponentIdentifier>()
        }
    }
    libraryNoticesDirectory = file("third-party/libs/notices")
    sourceFilesNotices.from(
        "assets/NOTICE",
        "base/third-party/observerList/NOTICE",
        "base/third-party/systemUtils/NOTICE",
        "device/third-party/versionCodes/NOTICE",
        "third-party/fontawesomeIcons/NOTICE",
        "third-party/tangoIcons/NOTICE",
        "third-party/themes/NOTICE",
    )
}

// Configure distribution (archive creation).
distributions {
    main {
        // The application plugin resets main distribution basename to applicationName, but I want to use it for shadow
        // configuration because it is the primary one. These tasks are disabled though.
        distributionBaseName = application.applicationName + "-noshadow"
    }
    this.shadow {
        distributionBaseName = application.applicationName
    }
}

tasks.shadowDistZip.configure {
    this.archiveClassifier = "noJRE"
}

// Using CopySpec.with(CopySpec) is the only way to add files into the distribution. Other approaches are:
// - old way of shadow.applicationDistribution no longer works after 2.0.0. The applicationDistribution from application
//   plugin still works though.
// - simply listing from/include in contents completely replaces the archive content
val additionalFiles = copySpec {
    from(projectDir)
    from(generateNotices.flatMap { it.outputNoticeFile })
    include("AUTHORS.md", "HISTORY", "LICENSE", "NOTICE", "README.md")
}
distributions.all { contents.with(additionalFiles) }

// Disable tasks from application plugin
disableTasks(
    ApplicationPlugin.TASK_DIST_ZIP_NAME,
    ApplicationPlugin.TASK_DIST_TAR_NAME,
    ApplicationPlugin.TASK_START_SCRIPTS_NAME,
)
disableTasks(
    tasks.assembleDist,
    tasks.installDist
)

// TARs aren't shipped, so disable it
disableTasks(tasks.shadowDistTar)

// Tweak windows start script, so it won't create console window.
// Based on https://stackoverflow.com/a/27472895/200754
// and http://mrhaki.blogspot.ru/2015/04/gradle-goodness-alter-start-scripts.html
tasks.startShadowScripts {
    doLast {
        windowsScript.writeBytes(
            windowsScript
                .readLines().joinToString(separator = "\r\n") { line ->
                    when {
                        line.contains("java.exe") -> line.replace("java.exe", "javaw.exe")
                        line.startsWith("\"%JAVA_EXE%\"") -> "start \"\" /b $line"
                        else -> line
                    }
                }.toByteArray()
        )
    }
}

tasks.named<ShadowJar>("shadowJar") {
    // NOTICES and licenses are included into ours NOTICE file manually.
    // Maven metadata isn't necessary also.
    exclude("**/NOTICE")
    exclude("**/LICENSE")
    exclude("META-INF/maven/**")

    minimize {
        // FlatLaf doesn't survive minimization
        exclude(dependency("com.formdev:flatlaf:.*"))
        // log4j appenders are referenced by name only
        exclude(dependency(libs.log4j.get()))
        exclude(dependency(libs.slf4j.reload4j.get()))
    }
}

// Configure publishing
bitbucket {
    repository = "android-log-viewer"
    username =
        providers.environmentVariable("BITBUCKET_USER").orElse(providers.gradleProperty("bitbucket.user"))
    applicationPassword =
        providers.environmentVariable("BITBUCKET_PASSWORD").orElse(providers.gradleProperty("bitbucket.password"))
}

tasks.register<UploadTask>("bitbucketUpload") {
    fileToUpload = tasks.shadowDistZip.flatMap { it.archiveFile }
}

installers {
    noJreDistribution = tasks.shadowDistZip.flatMap { it.archiveFile }

    vendor = "Mikhail Lopatkin"
    licenseFile = file("LICENSE")
    copyright = "Copyright 2011-2025 AndLogView authors"
    aboutUrl = "https://andlogview.mlopatkin.name"

    linux {
        icon = file("assets/andlogview.32.png")

        installerOptions = listOf(
            "--description", "Visual Log Viewer for Android logcat\n  " +
                    "AndLogView displays logs from a file or live from a connected device. " +
                    "Advanced filtering and search capabilities to navigate long and noisy logs.",
            "--linux-deb-maintainer", "me@mlopatkin.name",
            "--linux-app-category", "devel",
            "--linux-shortcut",
        )
        resourceDir = file("install/debian")

        extraContent {
            with(additionalFiles)
            into("extra")
        }

        extraContent {
            from(fileTree("assets") {
                include("andlogview.*.png")
                include("andlogview.svg")
                exclude("andlogview.32.png") // This one is added by the JPackage as "andlogview.png", see `icon` above.
                // Also exclude big macOS-only icons
                exclude("andlogview.512.png")
                exclude("andlogview.1024.png")
            })
        }
    }

    macos {
        displayAppName = "AndLogView"
        icon = file("assets/andlogview.icns")

        installerOptions = listOf(
            "--description", "Visual Log Viewer for Android logcat\n  " +
                    "AndLogView displays logs from a file or live from a connected device. " +
                    "Advanced filtering and search capabilities to navigate long and noisy logs.",
        )
    }

    windows {
        displayAppName = "AndLogView"
        icon = file("assets/andlogview.ico")

        installerOptions = listOf(
            "--description", "Visual Log Viewer for Android logcat\n  " +
                    "AndLogView displays logs from a file or live from a connected device. " +
                    "Advanced filtering and search capabilities to navigate long and noisy logs.",
            "--win-dir-chooser",
            "--win-shortcut",
            "--win-shortcut-prompt",
            "--win-menu",
            "--win-menu-group", "AndLogView",
            "--win-per-user-install",
            "--win-upgrade-uuid", "6d50b0e0-cabc-4d18-b7b2-d70806b0a01b"
        )

        extraContent {
            with(additionalFiles)
            into(".")
        }
    }
}

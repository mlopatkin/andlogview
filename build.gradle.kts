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
import name.mlopatkin.andlogview.building.buildLibs
import name.mlopatkin.andlogview.building.disableTasks
import name.mlopatkin.andlogview.building.theBuildDir
import name.mlopatkin.bitbucket.gradle.UploadTask
import name.mlopatkin.gradleplugins.licenses.License
import name.mlopatkin.gradleplugins.licenses.Resource

plugins {
    application

    alias(libs.plugins.shadow)
    alias(libs.plugins.bitbucket)
    alias(libs.plugins.jmh)

    id("name.mlopatkin.andlogview.building.build-environment")
    id("name.mlopatkin.andlogview.building.java-conventions")
    id("name.mlopatkin.andlogview.building.metadata")
    id("name.mlopatkin.gradleplugins.jpackage")
    id("name.mlopatkin.gradleplugins.licenses")
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

licenses {
    configuration = configurations.runtimeClasspath

    binaryDependency(
        libs.dagger.runtime,
        "Dagger",
        uri("https://dagger.dev"),
        License.apache2()
    )
    binaryDependency(
        "jakarta.inject:jakarta.inject-api",
        "Jakarta Dependency Injection",
        uri("https://github.com/eclipse-ee4j/injection-api"),
        License.apache2(fromJar("META-INF/LICENSE.txt")).withNotice(Resource.fromJar("META-INF/NOTICE.md"))
    )
    binaryDependency(
        "javax.inject:javax.inject",
        "javax.inject",
        uri("http://code.google.com/p/atinject/"),
        License.apache2()
    )

    binaryDependency(
        libs.ddmlib,
        "Android Tools ddmlib",
        uri("http://tools.android.com/"),
        License.apache2(fromJar("NOTICE")) // NOTICE includes the license
    )
    binaryDependency(
        "com.android.tools:annotations",
        "com.android.tools.annotations",
        uri("http://tools.android.com/"),
        License.apache2(fromJar("NOTICE")) // NOTICE includes the license
    )
    binaryDependency(
        "com.android.tools:common",
        "com.android.tools.common",
        uri("http://tools.android.com/"),
        License.apache2(fromJar("NOTICE")) // NOTICE includes the license
    )
    binaryDependency(
        "net.sf.kxml:kxml2",
        "kXML 2",
        uri("http://kxml.sourceforge.net/"),
        License.mit(fromFile("third-party/libs/notices/net.sf.kxml.kxml2.LICENSE"))
    )

    binaryDependency(
        libs.flatlaf.core,
        "FlatLaf",
        uri("https://github.com/JFormDesigner/FlatLaf"),
        License.apache2(fromJar("META-INF/LICENSE"))
    )

    binaryDependency(
        libs.flatlaf.extras,
        "FlatLaf Extras",
        uri("https://github.com/JFormDesigner/FlatLaf"),
        License.apache2(fromJar("META-INF/LICENSE"))
    )

    binaryDependency(
        "com.github.weisj:jsvg",
        "Jsvg",
        uri("https://github.com/weisJ/jsvg"),
        License.mit(fromJar("META-INF/LICENSE"))
    )

    binaryDependency(
        libs.gson,
        "Gson",
        uri("https://github.com/google/gson"),
        License.apache2()
    )

    binaryDependency(
        libs.guava,
        "Guava: Google Core Libraries for Java",
        uri("https://github.com/google/guava"),
        License.apache2(fromJar("META-INF/LICENSE"))
    )
    binaryDependency(
        "com.google.guava:failureaccess",
        "Guava InternalFutureFailureAccess and InternalFutures",
        uri("https://github.com/google/guava/failureaccess"),
        License.apache2()
    )

    binaryDependency(
        libs.jopt,
        "JOpt Simple",
        uri("http://jopt-simple.github.io/jopt-simple"),
        License.mit(fromFile("third-party/libs/notices/net.sf.jopt-simple.LICENSE"))
    )

    binaryDependency(
        libs.miglayout,
        "MiGLayout Swing",
        uri("http://www.miglayout.com/"),
        License.bsd3(fromFile("third-party/libs/notices/com.miglayout.miglayout-swing.LICENSE"))
    )
    binaryDependency(
        "com.miglayout:miglayout-core",
        "MiGLayout Core",
        uri("http://www.miglayout.com/"),
        License.bsd3(fromFile("third-party/libs/notices/com.miglayout.miglayout-core.LICENSE"))
    )

    binaryDependency(
        libs.slf4j.api,
        "SLF4J API Module",
        uri("http://www.slf4j.org/"),
        License.mit(fromJar("META-INF/LICENSE.txt"))
    )
    binaryDependency(
        libs.slf4j.reload4j,
        "SLF4J Reload4j Provider",
        uri("http://reload4j.qos.ch/"),
        License.mit(fromJar("META-INF/LICENSE.txt"))
    )
    binaryDependency(
        libs.log4j,
        "reload4j",
        uri("https://reload4j.qos.ch/"),
        License.apache2().withNotice(Resource.fromJar("META-INF/NOTICE"))
    )

    sourceDependency(
        "name/mlopatkin/andlogview/thirdparty/observerlist/ObserverList.java",
        "Chromium",
        "Chromium",
        uri("https://www.chromium.org/Home/"),
        License.bsd3(fromFile("base/third-party/observerList/LICENSE"))
    )

    sourceDependency(
        "name/mlopatkin/andlogview/thirdparty/systemutils/SystemUtils.java",
        "org.apache.commons:commons-lang3:3.2",
        "Apache Commons Lang",
        uri("https://commons.apache.org/proper/commons-lang/"),
        License.apache2().withNotice(fromFile("base/third-party/systemUtils/NOTICE"))
    )

    sourceDependency(
        "name/mlopatkin/andlogview/thirdparty/device/AndroidVersionCodes.java",
        "android",
        "Android Open Source Project",
        uri("https://source.android.com"),
        License.apache2(fromFile("device/third-party/versionCodes/NOTICE"))
    )

    sourceDependency(
        "name/mlopatkin/andlogview/ui/icons/fontawesome/*.svg",
        "fontAwesome",
        "Font Awesome",
        uri("https://fontawesome.com"),
        License.ccBy4(fromFile("third-party/fontawesomeIcons/NOTICE"))
    )

    sourceDependency(
        "name/mlopatkin/andlogview/ui/icons/legacy/*.png",
        "tango",
        "Tango Desktop Project",
        uri("http://tango.freedesktop.org/Tango_Desktop_Project"),
        License.publicDomain(fromFile("third-party/tangoIcons/NOTICE"))
    )

    sourceDependency(
        "name/mlopatkin/andlogview/ui/themes/LightFlatTheme.theme.json",
        "lightFlatTheme",
        "Light Flat Theme",
        uri("https://github.com/nerzhulart/LightFlatTheme"),
        License.mit(fromFile("third-party/themes/NOTICE"))
    )
}

//val generateNotices = tasks.register<GenerateNotices>("generateNotices") {
//    bundledDependencies = configurations.runtimeClasspath.flatMap { rtCp ->
//        rtCp.incoming.artifacts.resolvedArtifacts.map { artifacts ->
//            artifacts.map { artifact -> artifact.id.componentIdentifier }
//                .filterIsInstance<ModuleComponentIdentifier>()
//        }
//    }
//    libraryNoticesDirectory = file("third-party/libs/notices")
//    sourceFilesNotices.from(
//        "assets/NOTICE",
//        "base/third-party/observerList/NOTICE",
//        "base/third-party/systemUtils/NOTICE",
//        "device/third-party/versionCodes/NOTICE",
//        "third-party/fontawesomeIcons/NOTICE",
//        "third-party/tangoIcons/NOTICE",
//        "third-party/themes/NOTICE",
//    )
//}

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
    from(tasks.generateNotices.flatMap { it.noticeOutputFile })
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
    copyright = "Copyright 2011-2025 The AndLogView authors"
    aboutUrl = "https://andlogview.mlopatkin.name"

    linux {
        icon = file("assets/andlogview.32.png")

        installerOptions = listOf(
            "--description",
            "Visual Log Viewer for Android logcat\n  " +
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
        // On macOS the version must not start from `0.` or contain anything but dot-separated numbers.
        version = libs.versions.andlogview.map { "1.$it" }
        displayAppName = "AndLogView"
        icon = file("assets/andlogview.icns")

        installerOptions = listOf(
            "--description",
            "Visual Log Viewer for Android logcat\n  " +
                    "AndLogView displays logs from a file or live from a connected device. " +
                    "Advanced filtering and search capabilities to navigate long and noisy logs.",
        )
    }

    windows {
        // Do not use "-SNAPSHOT" versions here.
        version = libs.versions.andlogview

        displayAppName = "AndLogView"
        icon = file("assets/andlogview.ico")

        copyright = "(C) 2011-2025 The AndLogView authors"
        licenseFile = file("install/windows/LICENSE.rtf")

        imageOptions = listOf(
            "--description", "Visual log viewer for Android logcat"
        )

        installerOptions = listOf(
            "--description", "Visual log viewer for Android logcat",
            "--win-dir-chooser",
            "--win-shortcut",
            "--win-shortcut-prompt",
            "--win-menu",
            "--win-menu-group", "AndLogView",
            "--win-per-user-install",
            "--win-upgrade-uuid", "6d50b0e0-cabc-4d18-b7b2-d70806b0a01b"
        )

        resourceDir = file("install/windows")

        extraContent {
            with(additionalFiles)
            into(".")
        }
    }
}

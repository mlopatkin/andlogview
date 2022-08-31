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
import name.mlopatkin.andlogview.building.BuildEnvironment
import name.mlopatkin.andlogview.building.GenerateBuildMetadata
import name.mlopatkin.andlogview.building.GenerateNotices
import name.mlopatkin.andlogview.building.disableTasks
import name.mlopatkin.bitbucket.gradle.UploadTask
import net.ltgt.gradle.errorprone.errorprone
import java.util.*

plugins {
    java
    checkstyle
    idea
    application

    // Shadow plugin provides means to prepare a single-JAR distribution of the tool
    id("com.github.johnrengelman.shadow") version "7.1.2"
    // Errorprone plugin allows to configure Errorprone compiler
    id("net.ltgt.errorprone") version "2.0.2"
    // Bitbucket plugin allows publishing releases to the Bitbucket project's Downloads page
    id("name.mlopatkin.bitbucket") version "0.5-rc4"
    // Eclipse APT plugin configures annotation processors when importing the project into Eclipse
    id("com.diffplug.eclipse.apt") version "3.34.1"
    // JMH plugin allows building and running JMH benchmarks
    id("me.champeau.jmh") version "0.6.6"
    // Runtime plugin allows preparing runtime images with JDK included
    id("org.beryx.runtime") version "1.12.7"
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    val daggerVersion = "2.40.5"
    val flatlafVersion = "2.0"
    val nullawayVersion = "0.9.5"

    implementation("com.google.dagger:dagger:$daggerVersion")
    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("log4j:log4j:1.2.17")
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    implementation("com.android.tools.ddms:ddmlib:26.1.3")
    implementation("com.formdev:flatlaf:$flatlafVersion")
    implementation("com.formdev:flatlaf-extras:$flatlafVersion")

    compileOnly("org.checkerframework:checker-qual:3.21.1")

    val mockitoVersion = "4.2.0"

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("com.spotify:hamcrest-optional:1.2.0")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation(platform("org.junit:junit-bom:5.8.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")

    annotationProcessor("com.google.dagger:dagger-compiler:$daggerVersion")
    annotationProcessor("com.uber.nullaway:nullaway:$nullawayVersion")
    testAnnotationProcessor("com.uber.nullaway:nullaway:$nullawayVersion")
    jmhAnnotationProcessor("com.uber.nullaway:nullaway:$nullawayVersion")

    errorprone("com.google.errorprone:error_prone_core:2.10.0")
    errorproneJavac("com.google.errorprone:javac:9+181-r4173-1")
}

configurations {
    runtimeClasspath {
        // These annotations aren't needed in runtime.
        exclude(group = "com.google.code.findbugs", module = "jsr305")
        exclude(group = "org.checkerframework", module = "checker-qual")
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        exclude(group = "com.google.j2objc", module = "j2objc-annotations")
        exclude(group = "org.codehaus.mojo", module = "animal-sniffer-annotations")
        // Main Guava artifact includes listenable future, so the stub module isn't needed.
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
}

version = "0.22" + (if (BuildEnvironment.isSnapshotBuild()) "-SNAPSHOT" else "")

application {
    applicationName = "andlogview"
    mainClass.set("name.mlopatkin.andlogview.Main")
}

java {
    toolchain {
        this.languageVersion.set(JavaLanguageVersion.of(8))
    }
}

// Configure testing frameworks
tasks.withType<Test> {
    useJUnitPlatform()
}

jmh {
    humanOutputFile.set(file("$buildDir/reports/jmh/human.txt"))
}

checkstyle {
    toolVersion = "8.12"
}

// Configure sources
// TODO(mlopatkin) make this an output parameter of the BuildMetadata task
// Like annotationProcessor. Note java/main instead of Gradle's usual main/java.
val metadataBuildDir = "$buildDir/generated/sources/metadata/java/main"

sourceSets {
    main {
        java {
            srcDirs.clear()
            srcDir("src")
            srcDir(metadataBuildDir)
            srcDir("third-party/observerList/src")
            srcDir("third-party/styledLabel/src")
            srcDir("third-party/systemUtils/src")
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

// TODO(mlopatkin) make this a plugin
// Configure build metadata generator
val generateBuildMetadata = tasks.register<GenerateBuildMetadata>("generateBuildMetadata") {
    packageName = "name.mlopatkin.andlogview"
    className = "BuildInfo"
    into = file(metadataBuildDir)
    version = project.version.toString()
}

tasks.named("compileJava") {
    dependsOn(generateBuildMetadata)
}

idea.module.generatedSourceDirs.add(file(metadataBuildDir))

// Configure compilation warnings
tasks.withType<JavaCompile>().configureEach {
    // Configure javac warnings
    options.compilerArgs.addAll(listOf(
            "-Xlint:unchecked",
            "-Xlint:deprecation",
            "-Werror",  // Treat warnings as errors
    ))
    options.errorprone {
        // Configure ErrorProne
        errorproneArgs.addAll(
                "-Xep:JavaLangClash:OFF",
                "-Xep:MissingSummary:OFF",
                "-Xep:JavaUtilDate:OFF",
                "-Xep:UnusedVariable:OFF", // Incompatible with Dagger-generated class
                "-Xep:EmptyBlockTag:OFF",
        )
        // Configure NullAway
        option("NullAway:AnnotatedPackages", "name.mlopatkin")
        option("NullAway:AssertsEnabled", "true")
        option("NullAway:ExcludedClassAnnotations", "javax.annotation.Generated")
        option("NullAway:ExcludedFieldAnnotations",
                listOf("org.checkerframework.checker.nullness.qual.MonotonicNonNull",
                        "org.mockito.Mock,org.mockito.Captor",
                        "org.junit.jupiter.api.io.TempDir").joinToString(separator = ","))
        errorproneArgs.add("-Xep:NullAway:ERROR")
    }
}

val generateNotices = tasks.register<GenerateNotices>("generateNotices") {
    bundledDependencies.set(configurations.runtimeClasspath.flatMap { rtCp ->
        rtCp.incoming.artifacts.resolvedArtifacts.map { artifacts ->
            artifacts.map { artifact -> artifact.id.componentIdentifier }
        }
    })
    libraryNoticesDirectory.set(file("third-party/libs/notices"))
    sourceFilesNotices.from(
            "third-party/observerList/NOTICE",
            "third-party/systemUtils/NOTICE",
            "third-party/tangoIcons/NOTICE",
            "third-party/styledLabel/NOTICE",
    )
}

// Configure distribution (archive creation).
distributions {
    main {
        // The application plugin resets main distribution basename to applicationName, but I want to use it for shadow
        // configuration because it is the primary one. These tasks are disabled though.
        distributionBaseName.set(application.applicationName + "-noshadow")
    }
    this.shadow {
        distributionBaseName.set(application.applicationName)
    }
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
        "assembleDist",
        "installDist",
)

// TARs aren't shipped, so disable it
disableTasks("shadowDistTar")

// Tweak windows start script, so it won't create console window.
// Based on https://stackoverflow.com/a/27472895/200754
// and http://mrhaki.blogspot.ru/2015/04/gradle-goodness-alter-start-scripts.html
tasks.named<CreateStartScripts>("startShadowScripts") {
    doLast {
        windowsScript.writeBytes(windowsScript
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
        // FlatLaf doesn"t survive minimization
        exclude(dependency("com.formdev:flatlaf:.*"))
        // log4j appenders are referenced by name only
        exclude(dependency("log4j:log4j:1.2.17"))
    }
}

// Make distribution archives reproducible
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    // Set up consistent permissions on files. This is consistent with the permissions on Windows machines and the
    // Docker image.
    val permissionsMask = 0x1ED // octal 0755, aka rwxr-xr-x
    dirMode = permissionsMask
    eachFile {
        // Some files are executable, we want to keep them as such, thus we apply the mask.
        mode = mode and permissionsMask
    }
}

// Configure publishing
bitbucket {
    repository.set("android-log-viewer")
    username.set(
            providers.environmentVariable("BITBUCKET_USER").orElse(providers.gradleProperty("bitbucket.user")))
    applicationPassword.set(
            providers.environmentVariable("BITBUCKET_PASSWORD").orElse(providers.gradleProperty("bitbucket.password")))
}

tasks.register<UploadTask>("bitbucketUpload") {
    val shadowDistZip = tasks.named<AbstractArchiveTask>("shadowDistZip")
    fileToUpload.set(shadowDistZip.flatMap { it.archiveFile })
}

// Configure jpackage distribution
if (System.getProperty("os.name").toLowerCase(Locale.US).contains("linux")) {
    val jpackageJdkPath = javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_16.majorVersion))
    }.get().metadata.installationPath.toString()

    runtime {
        javaHome.set(jpackageJdkPath)
        options.addAll(
                "--strip-debug",
                "--no-header-files",
                "--no-man-pages",
                "--strip-native-commands",
                "--ignore-signing-information",
        )
        jpackage {
            jpackageHome = jpackageJdkPath
            installerType = "deb"
            installerOptions = listOf("--linux-shortcut")
            resourceDir = file("install/debian")
        }
    }
}

// Configure IDE support

// Modern gradle support in eclipse (Buildship) doesn't require this task, but it is brought in by eclipse.apt plugin
disableTasks("eclipse")
// TODO(mlopatkin) do we need this for modern Buildship versions?
// Order of tasks is important or generated metadata sources will not be imported
eclipse.synchronizationTasks("generateBuildMetadata", "eclipseJdtApt", "eclipseFactorypath", "eclipseJdt")


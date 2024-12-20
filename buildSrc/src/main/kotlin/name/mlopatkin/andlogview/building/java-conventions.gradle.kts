/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.building

import net.ltgt.gradle.errorprone.errorprone

plugins {
    java
    checkstyle
    `jvm-test-suite`

    // It is not possible to use a constant from the version catalog there
    id("net.ltgt.errorprone")

    id("name.mlopatkin.andlogview.building.reproducible-builds")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    compileOnly(buildLibs.checkerframeworkAnnotations)

    implementation(buildLibs.guava)

    testImplementation(buildLibs.guava)
    testImplementation(buildLibs.test.junit4)
    testImplementation(buildLibs.test.mockito.core)
    testImplementation(buildLibs.test.mockito.jupiter)
    testImplementation(buildLibs.test.hamcrest.hamcrest)
    testImplementation(buildLibs.test.hamcrest.optional)
    testImplementation(platform(buildLibs.test.assertj.bom))
    testImplementation(buildLibs.test.assertj.core)
    testImplementation(buildLibs.test.assertj.guava)
    testImplementation(platform(buildLibs.test.junit5.bom))
    testImplementation(buildLibs.test.junit5.jupiter)
    testRuntimeOnly(buildLibs.test.junit5.vintageEngine)

    annotationProcessor(buildLibs.build.jabel)
    annotationProcessor(buildLibs.build.nullaway)
    testAnnotationProcessor(buildLibs.build.jabel)
    testAnnotationProcessor(buildLibs.build.nullaway)

    errorprone(buildLibs.build.errorprone.core)
}

// Apply NullAway to JMH benchmark sources, if the plugin is available
pluginManager.withPlugin(buildLibs.plugins.jmh.pluginId) {
    dependencies {
        "jmhImplementation"(buildLibs.guava)
        "jmhAnnotationProcessor"(buildLibs.build.jabel)
        "jmhAnnotationProcessor"(buildLibs.build.nullaway)
    }
}

pluginManager.withPlugin("java-test-fixtures") {
    dependencies {
        "testFixturesImplementation"(buildLibs.guava)
        "testFixturesAnnotationProcessor"(buildLibs.build.jabel)
        "testFixturesAnnotationProcessor"(buildLibs.build.nullaway)
    }
}

val compileJdk = JdkVersion(buildLibs.versions.compileJdkVersion)
val runtimeJdk = JdkVersion(buildLibs.versions.runtimeJdkVersion)

java {
    toolchain {
        languageVersion = compileJdk.languageVersion
    }
}

checkstyle {
    toolVersion = buildLibs.versions.checkstyle.get()
}

// Configure testing frameworks
testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()

            targets.all {
                testTask.configure {
                    javaLauncher = javaToolchains.launcherFor {
                        languageVersion = runtimeJdk.languageVersion
                    }
                }
            }
        }
    }
}

// Configure compilation warnings
tasks.withType<JavaCompile>().configureEach {
    // Workaround for JMH plugin not respecting the toolchain
    javaCompiler.convention(javaToolchains.compilerFor(java.toolchain))

    sourceCompatibility = buildLibs.versions.sourceJavaVersion.get() // for the IDE support
    options.release = runtimeJdk.intProvider

    // Configure javac warnings
    options.compilerArgs.addAll(listOf(
            "-Xlint:all",
            "-Xlint:-serial,-processing",
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
        option("NullAway:ExcludedClassAnnotations", "javax.annotation.Generated,javax.annotation.processing.Generated")
        option("NullAway:ExcludedFieldAnnotations",
                listOf("org.checkerframework.checker.nullness.qual.MonotonicNonNull",
                        "org.mockito.Mock,org.mockito.Captor,org.mockito.Spy",
                        "org.junit.jupiter.api.io.TempDir").joinToString(separator = ","))
        errorproneArgs.add("-Xep:NullAway:ERROR")
    }
}

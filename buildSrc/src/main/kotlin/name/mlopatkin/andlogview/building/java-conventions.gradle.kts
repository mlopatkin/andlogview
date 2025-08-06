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
    `java-test-fixtures`
    `jvm-test-suite`

    // It is not possible to use a constant from the version catalog there
    id("net.ltgt.errorprone")

    id("name.mlopatkin.andlogview.building.reproducible-builds")
}

dependencies {
    implementation(buildLibs.slf4j.api)

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

    errorprone(buildLibs.build.errorprone.core)
}

// Set up common dependencies for every Java source set - main, test, jmh, test-fixtures.
sourceSets.withType<SourceSet> {
    dependencies {
        compileOnlyConfigurationName(buildLibs.checkerframeworkAnnotations)

        implementationConfigurationName(buildLibs.guava)

        annotationProcessorConfigurationName(buildLibs.build.jabel)
        annotationProcessorConfigurationName(buildLibs.build.nullaway)
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

    with(options) {
        encoding = "UTF-8"
        release = runtimeJdk.intProvider

        // Configure javac warnings
        compilerArgs.addAll(
            listOf(
                "-Xlint:all", // Enable everything
                // But silence some warnings we don't care about:
                // - serial is triggered by Swing-extending classes, but these are never serialized in the app.
                // - processing is too strict for the annotation processors we use.
                "-Xlint:-serial,-processing",
                "-Werror",  // Treat warnings as errors
            )
        )
        errorprone {
            disableWarningsInGeneratedCode = true

            disable(
                "AssignmentExpression", // I'm fine with assignment expressions.
                "EffectivelyPrivate", // I like to highlight the public members of private classes too.
                "EmptyBlockTag",
                "JavaLangClash",
                "JavaUtilDate",
                "MissingSummary",
            )

            // Configure NullAway
            option("NullAway:AnnotatedPackages", "name.mlopatkin")
            option("NullAway:AssertsEnabled", "true")
            option(
                "NullAway:ExcludedClassAnnotations",
                annotations(
                    "javax.annotation.Generated",
                    "javax.annotation.processing.Generated"
                )
            )
            option(
                "NullAway:ExcludedFieldAnnotations",
                annotations(
                    "org.checkerframework.checker.nullness.qual.MonotonicNonNull",
                    "org.junit.jupiter.api.io.TempDir",
                    "org.mockito.Captor",
                    "org.mockito.Mock",
                    "org.mockito.Spy",
                )
            )
            errorproneArgs.add("-Xep:NullAway:ERROR")
        }
    }
}

private fun annotations(vararg annotations: String): String = annotations.joinToString(separator = ",")

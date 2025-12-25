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

import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway

plugins {
    java
    checkstyle
    `java-test-fixtures`
    `jvm-test-suite`

    // It is not possible to use a constant from the version catalog there
    id("net.ltgt.errorprone")
    id("net.ltgt.nullaway")

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
    testRuntimeOnly(buildLibs.checkerframeworkAnnotations) {
        because("Java 8 needs type annotations on the classpath to avoid NPEs when reflecting")
    }
    testRuntimeOnly(buildLibs.test.junit5.vintageEngine)

    errorprone(buildLibs.build.errorprone.core)
}

// Set up common dependencies for every Java source set - main, test, jmh, test-fixtures.
sourceSets.withType<SourceSet> {
    dependencies {
        compileOnlyConfigurationName(buildLibs.checkerframeworkAnnotations)
        compileOnlyConfigurationName(buildLibs.errorprone.annotations)
        compileOnlyConfigurationName(buildLibs.jspecify)

        implementationConfigurationName(buildLibs.guava)

        annotationProcessorConfigurationName(buildLibs.build.bytebuddy) {
            because("upgrading Jabel's dependency to support Java 25")
        }
        annotationProcessorConfigurationName(buildLibs.guava) {
            because("upgrading NullAway's dependency to avoid warning on Java 25")
        }
        annotationProcessorConfigurationName(buildLibs.build.jabel)
        annotationProcessorConfigurationName(buildLibs.build.nullaway.processor)
    }
}

val cpiTask = tasks.register<PackageInfoCheckTask>("checkPackageInfos") {
    val srcDirRoots = sourceSets.main.map { it.java.srcDirTrees }
    this.sourceRoots.from(srcDirRoots.map { it.map(DirectoryTree::getDir) })
}

plugins.withType<LifecycleBasePlugin> {
    tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure {
        dependsOn(cpiTask)
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

        forkOptions {
            // Allow Jabel to attach its agent.
            jvmArgs = (jvmArgs ?: listOf()) + "-XX:+EnableDynamicAgentLoading"
        }

        // Generates metadata for reflection on method parameters.
        // This is required for Mockito to work properly on Java 8.
        // See https://github.com/junit-team/junit-framework/issues/3797 for inspiration.
        compilerArgs.add("-parameters")

        // Configure javac warnings
        compilerArgs.addAll(
            listOf(
                "-Xlint:all", // Enable everything
                // But silence some warnings we don't care about:
                // - options warns about Java 8 target on modern JVMs
                // - processing is too strict for the annotation processors we use.
                // - serial is triggered by Swing-extending classes, but these are never serialized in the app.
                // - this-escape is a common pattern in our code
                "-Xlint:-options,-processing,-serial,-this-escape",
                "-Werror",  // Treat warnings as errors
            )
        )

        errorprone {
            disableWarningsInGeneratedCode = true

            disable(
                "AssignmentExpression", // I'm fine with assignment expressions.
                "EffectivelyPrivate", // I like to highlight the public members of private classes too.
                "EmptyBlockTag",
                "InlineMeInliner", // Incorrect suggestions of Java 11+ APIs
                "JavaLangClash",
                "JavaUtilDate",
                "MissingSummary",
                "NullArgumentForNonNullParameter", // Breaks compilation
            )

            nullaway {
                annotatedPackages = listOf("name.mlopatkin")
                isAssertsEnabled = true
                isJSpecifyMode = true

                excludedClassAnnotations = listOf(
                    "javax.annotation.Generated",
                    "javax.annotation.processing.Generated",
                )

                excludedFieldAnnotations = listOf(
                    "org.junit.jupiter.api.io.TempDir",
                    "org.junit.runners.Parameterized.Parameter",
                    "org.mockito.Captor",
                    "org.mockito.Mock",
                    "org.mockito.Spy",
                    "org.openjdk.jmh.annotations.Param",
                )

                customInitializerAnnotations = listOf(
                    "org.openjdk.jmh.annotations.Setup"
                )

                // NullAway doesn't support parametric nullness properly, and treats the code as NonNull.
                // Unfortunately, this is sometimes problematic.
                customNullableAnnotations = listOf(
                    "com.google.common.collect.ParametricNullness",
                    "com.google.common.util.concurrent.ParametricNullness",
                )

                severity = CheckSeverity.ERROR
            }
        }
    }
}

afterEvaluate {
    // Use afterEvaluate to make sure all configurations have their roles set.

    configurations.all {
        if (isCanBeResolved && !isCanBeConsumed) {
            resolutionStrategy.dependencySubstitution {
                substitute(module(buildLibs.jspecify.get().toString())).using(project(":jspecify"))
            }
        }
    }
}

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

    // It is not possible to use a constant from the version catalog there
    id("com.diffplug.eclipse.apt")
    id("net.ltgt.errorprone")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    compileOnly(buildLibs.checkerframeworkAnnotations)

    testImplementation(buildLibs.test.junit4)
    testImplementation(buildLibs.test.mockito.core)
    testImplementation(buildLibs.test.mockito.jupiter)
    testImplementation(buildLibs.test.hamcrest.all)
    testImplementation(buildLibs.test.hamcrest.optional)
    testImplementation(buildLibs.test.assertj)
    testImplementation(platform(buildLibs.test.junit5.bom))
    testImplementation(buildLibs.test.junit5.jupiter)
    testRuntimeOnly(buildLibs.test.junit5.vintageEngine)

    annotationProcessor(buildLibs.build.jabel)
    annotationProcessor(buildLibs.build.nullaway)
    testAnnotationProcessor(buildLibs.build.jabel)
    testAnnotationProcessor(buildLibs.build.nullaway)

    errorprone(buildLibs.build.errorprone.core)
    errorproneJavac(buildLibs.build.errorprone.javac)
}

// Apply NullAway to JMH benchmark sources, if the plugin is available
pluginManager.withPlugin(buildLibs.plugins.jmh.pluginId) {
    dependencies {
        "jmhAnnotationProcessor"(buildLibs.build.jabel)
        "jmhAnnotationProcessor"(buildLibs.build.nullaway)
    }
}

pluginManager.withPlugin("java-test-fixtures") {
    dependencies {
        "testFixturesAnnotationProcessor"(buildLibs.build.jabel)
        "testFixturesAnnotationProcessor"(buildLibs.build.nullaway)
    }
}

val compileJdk = JdkVersion(buildLibs.versions.compileJdkVersion)
val runtimeJdk = JdkVersion(buildLibs.versions.runtimeJdkVersion)

java {
    toolchain {
        languageVersion.set(compileJdk.languageVersion)
    }
}

checkstyle {
    toolVersion = buildLibs.versions.checkstyle.get()
}

// Configure testing frameworks
tasks.withType<Test> {
    useJUnitPlatform()

    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(runtimeJdk.languageVersion)
    })
}

// Configure compilation warnings
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = buildLibs.versions.sourceJavaVersion.get() // for the IDE support
    options.release.set(runtimeJdk.intProvider)

    // Configure javac warnings
    options.compilerArgs.addAll(listOf(
            "-Xlint:unchecked",
            "-Xlint:rawtypes",
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
                "-Xep:UnnecessaryLambda:OFF",
        )
        // Configure NullAway
        option("NullAway:AnnotatedPackages", "name.mlopatkin")
        option("NullAway:AssertsEnabled", "true")
        option("NullAway:ExcludedClassAnnotations", "javax.annotation.Generated,javax.annotation.processing.Generated")
        option("NullAway:ExcludedFieldAnnotations",
                listOf("org.checkerframework.checker.nullness.qual.MonotonicNonNull",
                        "org.mockito.Mock,org.mockito.Captor",
                        "org.junit.jupiter.api.io.TempDir").joinToString(separator = ","))
        errorproneArgs.add("-Xep:NullAway:ERROR")
    }
}

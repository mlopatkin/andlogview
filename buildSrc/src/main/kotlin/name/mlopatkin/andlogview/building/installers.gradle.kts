/*
 * Copyright 2025 the Andlogview authors
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

plugins {
    id("name.mlopatkin.andlogview.building.build-environment")

    id("org.beryx.runtime")
}

interface PackageExtension {
    val resourceDir: DirectoryProperty
    val installerOptions: ListProperty<String>
}

abstract class InstallerExtension @Inject constructor(objects: ObjectFactory) {
    val linux: PackageExtension = objects.newInstance<PackageExtension>()

    @Suppress("unused")
    fun linux(configure: PackageExtension.() -> Unit) = configure(linux)
}

val installersExtension = extensions.create<InstallerExtension>("installers")

val unsupportedCcTaskTypes = listOf(
    org.beryx.runtime.JPackageImageTask::class,
    org.beryx.runtime.JPackageTask::class,
    org.beryx.runtime.JreTask::class,
)

unsupportedCcTaskTypes.forEach {
    tasks.withType(it).configureEach {
        notCompatibleWithConfigurationCache("JLink plugin is not yet CC-compatible")
    }
}

val jdkForJpackage: Provider<String> =
    javaToolchains.compilerFor(java.toolchain).map { it.metadata.installationPath.asFile.path }

// Gradle doesn't generate accessors for sibling plugins.
val buildEnvironment = extensions.getByType<BuildEnvironment>()

runtime {
    javaHome = jdkForJpackage

    options.addAll(
        "--strip-debug",
        "--no-header-files",
        "--no-man-pages",
        "--strip-native-commands",
        "--ignore-signing-information",
        "--compress", "1"
    )

    jpackage {
        // There is no lazy API to set jpackageHome.
        afterEvaluate {
            jpackageHome = jdkForJpackage.get()

            // Platform-specific configuration of jpackage
            if (buildEnvironment.isLinux) {
                installerType = "deb"
                with(installersExtension) {
                    installerOptions = linux.installerOptions.get()
                    resourceDir = linux.resourceDir.get().asFile
                }
                installerOutputDir = layout.buildDirectory.dir("distributions").get().asFile
            }
        }
    }
}

val linuxInstaller = tasks.register("linuxInstallers") {
    dependsOn(":jpackage")
    val isLinux = buildEnvironment.isLinux
    onlyIf("Can only run when running on Linux") { isLinux }

    group = "distribution"
    description = "Builds Linux installers with bundled Java runtime (only on Linux)"
}

tasks.register("installers") {
    dependsOn(linuxInstaller)

    group = "distribution"
    description = "Builds all installers for the current platform"
}

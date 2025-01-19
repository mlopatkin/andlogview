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

import org.beryx.runtime.JPackageImageTask
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    id("name.mlopatkin.andlogview.building.build-environment")

    id("org.beryx.runtime")
}

/**
 * A platform-specific distribution configuration.
 */
@Suppress("unused")
abstract class PackageExtension @Inject constructor(
    private val fsOps: FileSystemOperations,
) {
    internal val contentResources = fsOps.copySpec()

    /**
     * Options to build the runtime image. Cannot carry task dependencies.
     */
    abstract val imageOptions: ListProperty<String>

    /**
     * Options to configure the installer. Cannot carry task dependencies.
     */
    abstract val installerOptions: ListProperty<String>

    /**
     * Resource directory to override installer stuff. Cannot carry task dependencies.
     *
     * @see <a href="https://docs.oracle.com/en/java/javase/17/jpackage/override-jpackage-resources.html#GUID-1B718F8B-B68D-4D46-B1DB-003D7729AAB6">JPackage docs</a>
     */
    abstract val resourceDir: DirectoryProperty

    /**
     * Extra application content to be packaged into the distribution. The exact location is platform-dependent. On
     * Linux, it will be installed into `/opt/andlogview/lib/`, alongside the icon and launcher.
     *
     * Can carry task dependencies.
     *
     * @param configuration the copy spec configuration block
     */
    fun extraContent(configuration: CopySpec.() -> Unit) {
        contentResources.with(fsOps.copySpec().apply(configuration))
    }
}

/**
 * Installer configuration. Available through `installers {}` block.
 */
abstract class InstallerExtension @Inject constructor(objects: ObjectFactory) {
    /**
     * Linux distribution configuration.
     */
    val linux: PackageExtension = objects.newInstance<PackageExtension>()

    /**
     * Configures Linux distribution.
     */
    @Suppress("unused")
    fun linux(configure: PackageExtension.() -> Unit) = configure(linux)
}

val installersExtension = extensions.create<InstallerExtension>("installers")

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
                    imageOptions = linux.imageOptions.get()
                    resourceDir = linux.resourceDir.locationOnly.toFile()
                }
                installerOutputDir = layout.buildDirectory.dir("distributions").toFile()
            }
        }
    }
}

/**
 * An intermediate collector of extra resources specified with [PackageExtension.extraContent].
 */
val copyResources = tasks.register<Sync>("copyJpackageResources") {
    // This is an intermediate task to collect all extra resources. We don't need to copy them, strictly speaking,
    // but I don't see how I can convert a copy spec into inputs of an arbitrary task.
    with(installersExtension.linux.contentResources)

    into(layout.buildDirectory.dir("tmp/$name"))
}

/**
 * A path where application content is stored when building the app image.
 */
val JPackageImageTask.contentOutput: Provider<File>
    // TODO(mlopatkin) The extra content location is platform-dependent. See
    //  https://github.com/openjdk/jdk/blob/master/src/jdk.jpackage/share/classes/jdk/jpackage/internal/ApplicationLayout.java#L146
    get() = provider { File(jpackageData.imageOutputDirOrDefault, "${jpackageData.imageNameOrDefault}/lib/") }


tasks.named<JPackageImageTask>(RuntimePluginExt.TASK_NAME_JPACKAGE_IMAGE) {
    val fsOps = serviceOf<FileSystemOperations>()
    val outputDir = contentOutput

    inputs.files(copyResources)

    doLast {
        fsOps.copy {
            from(copyResources)
            into(outputDir)
        }
    }
}

// Register entry points for distributions.
val linuxInstaller = tasks.register("linuxInstallers") {
    dependsOn(":jpackage")
    val isLinux = buildEnvironment.isLinux
    onlyIf("Can only run when running on Linux") { isLinux }

    group = "distribution"
    description = "Builds Linux installers with bundled Java runtime (only on Linux)"
}

tasks.register("installers") {
    // TODO(mlopatkin) this should also build the JAR distribution (aka noJre).
    dependsOn(linuxInstaller)

    group = "distribution"
    description = "Builds all installers for the current platform"
}

// Ensure configuration cache failures from runtime plugin don't break the build.
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

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
     * The primary App icon. Cannot carry task dependencies.
     */
    abstract val icon: RegularFileProperty

    /**
     * A name of the application, visible to the user. It is used as base for the launcher name, menu and desktop
     * entries.
     */
    abstract val displayAppName: Property<String>

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
     * Linux, it will be installed into `/opt/andlogview/lib/`, alongside the icon and launcher. On Windows it is
     * installed into the root of the app installation directory.
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
@Suppress("MemberVisibilityCanBePrivate")
abstract class InstallerExtension @Inject constructor(
    objects: ObjectFactory,
    private val buildEnvironment: BuildEnvironment
) {
    /**
     * Path to the vintage distribution that doesn't contain Java runtime.
     */
    abstract val noJreDistribution: RegularFileProperty

    /**
     * Application vendor. Shown in the package and application metadata.
     */
    abstract val vendor: Property<String>

    /**
     * Path to the license file. Embedded into packages or shown as an EULA in the installer.
     */
    abstract val licenseFile: RegularFileProperty

    /**
     * A copyright string. Shown in the package and application metadata.
     */
    abstract val copyright: Property<String>

    /**
     * An "about" URL. Used somewhere in installer metadata.
     */
    abstract val aboutUrl: Property<String>

    /**
     * Linux distribution configuration.
     */
    val linux: PackageExtension = objects.newInstance<PackageExtension>()

    /**
     * Configures Linux distribution.
     */
    @Suppress("unused")
    fun linux(configure: PackageExtension.() -> Unit) = configure(linux)

    /**
     * MacOS distribution configuration.
     */
    val macos: PackageExtension = objects.newInstance<PackageExtension>()

    /**
     * Configures macOS distribution.
     */
    @Suppress("unused")
    fun macos(configure: PackageExtension.() -> Unit) = configure(macos)

    /**
     * Windows distribution configuration.
     */
    val windows: PackageExtension = objects.newInstance<PackageExtension>()

    /**
     * Configures Windows distribution.
     */
    @Suppress("unused")
    fun windows(configure: PackageExtension.() -> Unit) = configure(windows)

    internal val forCurrentPlatform: PackageExtension
        get() = when {
            buildEnvironment.isLinux -> linux
            buildEnvironment.isMacos -> macos
            buildEnvironment.isWindows -> windows
            else -> throw IllegalArgumentException("Unsupported Build Platform")
        }

    internal val platforms: List<PackageExtension> = listOf(linux, macos, windows)
}

// Gradle doesn't generate accessors for sibling plugins.
val buildEnvironment = extensions.getByType<BuildEnvironment>()

val installersExtension = extensions.create<InstallerExtension>("installers", buildEnvironment).apply {
    val nameProvider = provider { project.name }
    platforms.forEach {
        it.displayAppName.convention(nameProvider)
    }
}

val jdkForJpackage: Provider<String> =
    javaToolchains.compilerFor(java.toolchain).map { it.metadata.installationPath.asFile.path }

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

            with(installersExtension) {
                imageName = forCurrentPlatform.displayAppName.get()
                installerName = forCurrentPlatform.displayAppName.get()

                imageOptions = buildList {
                    withOptionalSwitch("--icon", forCurrentPlatform.icon.asPath)

                    addAll(forCurrentPlatform.imageOptions.get())
                }

                installerOptions = buildList {
                    withOptionalSwitch("--vendor", vendor)
                    withOptionalSwitch("--license-file", licenseFile.asPath)
                    withOptionalSwitch("--copyright", copyright)
                    withOptionalSwitch("--about-url", aboutUrl)

                    addAll(forCurrentPlatform.installerOptions.get())
                }

                forCurrentPlatform.resourceDir.ifPresent {
                    resourceDir = it.asFile
                }

                // Platform-specific configuration of jpackage
                when {
                    buildEnvironment.isLinux -> {
                        installerType = "deb"
                    }

                    buildEnvironment.isMacos -> {
                        installerType = "dmg"
                        // macOS version is integers only, starting with non-zero. No SNAPSHOT, no 0.x for you.
                        appVersion = "1." + project.version.toString().replace("-SNAPSHOT", "")

                        // Add the icon to the installer too
                        installerOptions = installerOptions + optionalSwitch("--icon", forCurrentPlatform.icon.asPath)
                    }

                    buildEnvironment.isWindows -> {
                        installerType = "exe"
                        // Windows Version must be at most three dot-separated numbers. We cannot fit SNAPSHOT there.
                        appVersion = project.version.toString().replace("-SNAPSHOT", "")
                    }
                }
            }
        }
    }
}


val Provider<out FileSystemLocation>.asPath: Provider<String>
    get() = map { it.asFile.path }


fun optionalSwitch(switch: String, value: Provider<out String>): List<String> {
    return value.map { listOf(switch, it) }.orElse(listOf()).get()
}


fun MutableList<String>.withOptionalSwitch(switch: String, value: Provider<out String>) {
    addAll(optionalSwitch(switch, value))
}


inline fun <T> Provider<out T>.ifPresent(block: (T) -> Unit) {
    if (isPresent) {
        block(get())
    }
}

/**
 * An intermediate collector of extra resources specified with [PackageExtension.extraContent].
 */
val copyResources = tasks.register<Sync>("copyJpackageResources") {
    // This is an intermediate task to collect all extra resources. We don't need to copy them, strictly speaking,
    // but I don't see how I can convert a copy spec into inputs of an arbitrary task.
    with(installersExtension.forCurrentPlatform.contentResources)

    into(layout.buildDirectory.dir("tmp/$name"))
}

/**
 * A path where application content is stored when building the app image.
 */
val JPackageImageTask.contentOutput: Provider<File>
    // TODO(mlopatkin) The extra content location is platform-dependent. See
    //  https://github.com/openjdk/jdk/blob/master/src/jdk.jpackage/share/classes/jdk/jpackage/internal/ApplicationLayout.java#L146
    get() = provider {
        val imageDir = File(jpackageData.imageOutputDirOrDefault, jpackageData.imageNameOrDefault)
        when {
            buildEnvironment.isLinux -> File(imageDir, "lib/")
            buildEnvironment.isWindows -> imageDir
            buildEnvironment.isMacos -> File(imageDir, "Contents/")
            else -> throw IllegalArgumentException("Unsupported platform")
        }
    }


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

/**
 * An ad-hoc replacement for Copy that only marks the copied files as the output.
 */
@Suppress("LeakingThis")
abstract class CopyInstallers : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val installers: ConfigurableFileCollection

    @get:Internal
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val appName: Property<String>

    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val platform: Property<String>

    @get:Input
    abstract val platformArchitecture: Property<String>

    @get:OutputFiles
    val copiedInstallers: FileCollection = project.files(installers.elements.zip(outputDirectory) { files, dst ->
        files.map { dst.file(decorateInstallerName(it.asFile)) }.toList()
    })

    @get:Inject
    abstract val fsOps: FileSystemOperations

    init {
        appName.convention(project.provider { project.name })
        version.convention(project.provider { project.version.toString() })
    }

    @TaskAction
    fun copyInstallers() {
        fsOps.copy {
            from(installers)
            into(outputDirectory)

            eachFile {
                name = decorateInstallerName(file)
            }
        }
    }

    private fun decorateInstallerName(originalName: File): String {
        require(!originalName.name.contains("noJRE")) {
            "Must not copy the noJRE installer through this task"
        }
        val extension = originalName.extension
        val version = this.version.get()
        val appName = this.appName.get()
        val platform = this.platform.get()
        val qualifier = this.platformArchitecture.get()

        return "$appName-$version-$platform-$qualifier.$extension"
    }
}

tasks.withType<CopyInstallers> {
    platformArchitecture = buildEnvironment.architecture
    platform = buildEnvironment.platformName
}

// Register entry points for distributions.
val linuxInstaller = tasks.register<CopyInstallers>("linuxInstallers") {
    // We have to copy the output, because jpackage cleans up its destination dir if it doesn't contain the image.
    dependsOn(":jpackage")
    val isLinux = buildEnvironment.isLinux
    onlyIf("Can only run when running on Linux") { isLinux }

    group = "distribution"
    description = "Builds Linux installers with bundled Java runtime (only on Linux)"

    outputDirectory = theBuildDir.dir("distributions")

    installers.from(fileTree(theBuildDir.dir("jpackage")) {
        include("*.deb")
        include("*.rpm")
    })
}

val macosInstaller = tasks.register<CopyInstallers>("macosInstallers") {
    // We have to copy the output, because jpackage cleans up its destination dir if it doesn't contain the image.
    dependsOn(":jpackage")
    val isMacos = buildEnvironment.isMacos
    onlyIf("Can only run when running on macOS") { isMacos }

    group = "distribution"
    description = "Builds macOS installers with bundled Java runtime (only on macOS)"

    outputDirectory = theBuildDir.dir("distributions")

    installers.from(fileTree(theBuildDir.dir("jpackage")) {
        include("*.dmg")
        include("*.pkg")
    })
}

val windowsInstaller = tasks.register<CopyInstallers>("windowsInstallers") {
    // We have to copy the output, because jpackage cleans up its destination dir if it doesn't contain the image.
    dependsOn(":jpackage")
    val isWindows = buildEnvironment.isWindows
    onlyIf("Can only run when running on Windows") { isWindows }

    group = "distribution"
    description = "Builds Windows installers with bundled Java runtime (only on Windows)"

    outputDirectory = theBuildDir.dir("distributions")

    installers.from(fileTree(theBuildDir.dir("jpackage")) {
        include("*.msi")
        include("*.exe")
    })
}

val noJreInstaller = tasks.register("noJreInstaller") {
    dependsOn(installersExtension.noJreDistribution)

    group = "distribution"
    description = "Builds a universal installer without Java runtime. Can run on any platform"
}

tasks.register("installers") {
    dependsOn(linuxInstaller, macosInstaller, noJreInstaller, windowsInstaller)

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

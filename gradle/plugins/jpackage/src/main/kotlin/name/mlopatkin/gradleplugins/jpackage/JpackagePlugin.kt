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

package name.mlopatkin.gradleplugins.jpackage

import org.beryx.runtime.JPackageImageTask
import org.beryx.runtime.JPackageTask
import org.beryx.runtime.JreTask
import org.beryx.runtime.data.RuntimePluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

/**
 * A wrapper for the `org.beryx.runtime` plugin. Provides platform-specific configuration options with a nicer DSL.
 *
 * @see InstallerExtension
 */
@Suppress("unused")
abstract class JpackagePlugin : Plugin<Project> {
    @get:Inject
    protected abstract val objectFactory: ObjectFactory

    @get:Inject
    protected abstract val fsOps: FileSystemOperations

    @get:Inject
    protected abstract val execOps: ExecOperations

    @Suppress("LeakingThis")
    private val buildEnvironment = objectFactory.newInstance<BuildEnvironment>()

    override fun apply(project: Project) {
        project.pluginManager.withPlugin("java") {
            project.configureThisPlugin()
        }
    }

    private fun Project.configureThisPlugin() {
        pluginManager.apply("org.beryx.runtime")

        val java = extensions.getByType<JavaPluginExtension>()

        val installers = extensions.create<InstallerExtension>("installers", buildEnvironment).apply {
            version = provider { project.version.toString() }
            val defaultToolchain = java.toolchain
            javaToolchain {
                languageVersion.convention(defaultToolchain.languageVersion)
                implementation.convention(defaultToolchain.implementation)
                vendor.convention(defaultToolchain.vendor)
            }

            val nameProvider = provider { project.name }
            platforms.forEach {
                it.version.convention(version)
                it.copyright.convention(copyright)
                it.displayAppName.convention(nameProvider)
                it.licenseFile.convention(licenseFile)
            }
        }

        val runtime = extensions.getByType<RuntimePluginExtension>()
        afterEvaluate { configureBadassRuntimePluginLater(runtime, installers) }

        addImagePostProcessingStep(installers.forCurrentPlatform.contentResources)

        val jpackageOutputDir = runtime.jpackageData.map { it.installerOutputDirOrDefault }

        // Register entry points for distributions.
        val linuxInstaller =
            createPlatformInstallerTask(
                "linuxInstallers",
                "Linux",
                buildEnvironment.isLinux,
                jpackageOutputDir,
                "*.deb",
                "*.rpm"
            )
        val macosInstaller =
            createPlatformInstallerTask(
                "macosInstallers",
                "macOS",
                buildEnvironment.isMacos,
                jpackageOutputDir,
                "*.dmg",
                "*.pkg"
            )
        val windowsInstaller =
            createPlatformInstallerTask(
                "windowsInstallers",
                "Windows",
                buildEnvironment.isWindows,
                jpackageOutputDir,
                "*.exe",
                "*.msi"
            )

        val noJreInstaller = tasks.register("noJreInstaller") {
            dependsOn(installers.noJreDistribution)

            group = "distribution"
            description = "Builds a universal installer without Java runtime. Can run on any platform"
        }

        tasks.register("installers") {
            dependsOn(linuxInstaller, macosInstaller, noJreInstaller, windowsInstaller)

            group = "distribution"
            description = "Builds all installers for the current platform"
        }

        markCcIncompatibleTasks()
    }

    private fun Project.configureBadassRuntimePluginLater(
        runtime: RuntimePluginExtension,
        installers: InstallerExtension,
    ) {
        runtime.run {
            javaHome = installers.jdkPath

            modules = installers.modules

            options.addAll(
                "--strip-debug",
                "--no-header-files",
                "--no-man-pages",
                "--strip-native-commands",
                "--ignore-signing-information",
                "--compress", "1"
            )

            jpackage {
                jpackageHome = installers.jdkPath.get()

                with(installers) {
                    imageName = forCurrentPlatform.displayAppName.get()
                    installerName = forCurrentPlatform.displayAppName.get()
                    appVersion = forCurrentPlatform.version.get()

                    imageOptions = buildList {
                        withOptionalSwitch("--icon", forCurrentPlatform.icon.asPath)
                        withOptionalSwitch("--copyright", forCurrentPlatform.copyright)

                        addAll(forCurrentPlatform.imageOptions.get())
                    }

                    installerOptions = buildList {
                        withOptionalSwitch("--vendor", vendor)
                        withOptionalSwitch("--license-file", forCurrentPlatform.licenseFile.asPath)
                        withOptionalSwitch("--copyright", forCurrentPlatform.copyright)
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

                            // Add the icon to the installer too
                            installerOptions =
                                installerOptions + optionalSwitch("--icon", forCurrentPlatform.icon.asPath)
                        }

                        buildEnvironment.isWindows -> {
                            installerType = "exe"
                        }
                    }

                    tasks.named<JPackageImageTask>(RuntimePluginExt.TASK_NAME_JPACKAGE_IMAGE) {
                        withOptionalInput("icon", forCurrentPlatform.icon)
                    }

                    tasks.named<JPackageTask>(RuntimePluginExt.TASK_NAME_JPACKAGE) {
                        withOptionalInput("icon", forCurrentPlatform.icon)
                        withOptionalInput("license", forCurrentPlatform.licenseFile)
                    }
                }
            }
        }
    }

    private fun Project.addImagePostProcessingStep(contentResources: CopySpec) {
        val copyResources = tasks.register<Sync>("copyJpackageResources") {
            // This is an intermediate task to collect all extra resources. We don't need to copy them, strictly
            // speaking, but I don't see how I can convert a copy spec into inputs of an arbitrary task.
            with(contentResources)

            into(theBuildDir.dir("tmp/$name"))
        }

        tasks.named<JPackageImageTask>(RuntimePluginExt.TASK_NAME_JPACKAGE_IMAGE) {
            val fsOps = fsOps // don't capture the whole plugin
            val outputDir = contentOutput

            inputs.files(copyResources)

            doLast {
                fsOps.copy {
                    from(copyResources)
                    into(outputDir)
                }
            }
        }
    }


    /**
     * A path where application content is stored when building the app image.
     */
    private val JPackageImageTask.contentOutput: Provider<File>
        // See https://github.com/openjdk/jdk/blob/master/src/jdk.jpackage/share/classes/jdk/jpackage/internal/ApplicationLayout.java#L146
        get() = project.provider {
            val imageDir = File(jpackageData.imageOutputDirOrDefault, jpackageData.imageNameOrDefault)
            when {
                buildEnvironment.isLinux -> File(imageDir, "lib/")
                buildEnvironment.isWindows -> imageDir
                buildEnvironment.isMacos -> File(imageDir, "Contents/")
                else -> throw IllegalArgumentException("Unsupported platform")
            }
        }

    private fun Project.createPlatformInstallerTask(
        taskName: String,
        platformName: String,
        isEnabled: Boolean,
        jpackageOutputDir: Provider<File>,
        vararg distFilePatterns: String
    ): TaskProvider<CopyInstallers> {
        return tasks.register<CopyInstallers>(taskName) {
            if (isEnabled) {
                platformArchitecture = buildEnvironment.architecture
                platform = buildEnvironment.platformName
            }

            // We have to copy the output, because jpackage cleans up its destination dir if it doesn't contain the
            // image.
            dependsOn(RuntimePluginExt.TASK_NAME_JPACKAGE)
            onlyIf("Can only run when running on $platformName") { isEnabled }

            group = "distribution"
            description = "Builds $platformName installers with bundled Java runtime (only on $platformName)"

            outputDirectory = theBuildDir.dir("distributions")

            installers.from(fileTree(jpackageOutputDir) {
                distFilePatterns.forEach {
                    include(it)
                }
            })
        }
    }

    private fun Project.markCcIncompatibleTasks() {
        // Ensure configuration cache failures from runtime plugin don't break the build.
        val unsupportedCcTaskTypes = listOf(
            JPackageImageTask::class,
            JPackageTask::class,
            JreTask::class,
        )

        unsupportedCcTaskTypes.forEach {
            tasks.withType(it).configureEach {
                notCompatibleWithConfigurationCache("JLink plugin is not yet CC-compatible")
            }
        }
    }
}

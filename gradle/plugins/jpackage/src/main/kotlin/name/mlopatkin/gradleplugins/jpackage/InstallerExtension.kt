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

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.internal.DefaultToolchainSpec
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

/**
 * Installer configuration. Available through `installers {}` block.
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class InstallerExtension @Inject constructor(
    objects: ObjectFactory,
    private val buildEnvironment: BuildEnvironment
) {
    @get:Inject
    protected abstract val javaToolchains: JavaToolchainService

    /**
     * Java Toolchain to find `jpackage` in. By default, the toolchain of the `java` extension is used.
     */
    val javaToolchain: JavaToolchainSpec = objects.newInstance<DefaultToolchainSpec>()

    /**
     * Configures java toolchain to find `jpackage` in. By default, the toolchain of the `java` extension is used.
     */
    @Suppress("unused")
    fun javaToolchain(configure: JavaToolchainSpec.() -> Unit) = configure(javaToolchain)

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
     * The version of the application. Can be overridden per-platform, uses project's version by default.
     */
    abstract val version: Property<String>

    /**
     * Linux distribution configuration.
     */
    val linux: PackageExtension = objects.newInstance()

    /**
     * Configures Linux distribution.
     */
    @Suppress("unused")
    fun linux(configure: PackageExtension.() -> Unit) = configure(linux)

    /**
     * MacOS distribution configuration.
     */
    val macos: PackageExtension = objects.newInstance()

    /**
     * Configures macOS distribution.
     */
    @Suppress("unused")
    fun macos(configure: PackageExtension.() -> Unit) = configure(macos)

    /**
     * Windows distribution configuration.
     */
    val windows: PackageExtension = objects.newInstance()

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

    internal val jdkPath: Provider<String>
        get() = javaToolchains.locationOf(javaToolchain).asPath
}

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

import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

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
     * A copyright string. Shown in the package and application metadata.
     * <p>
     * By default, uses [InstallerExtension.copyright].
     */
    abstract val copyright: Property<String>

    /**
     * A license file. Shown in the installer.
     * <p>
     * By default, uses [InstallerExtension.licenseFile].
     */
    abstract val licenseFile: RegularFileProperty

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

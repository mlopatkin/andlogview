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

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

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

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

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists

/**
 * Verifies that all packages in the source root have `package-info` files.
 */
@CacheableTask
abstract class PackageInfoCheckTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:SkipWhenEmpty
    abstract val sourceRoots: ConfigurableFileCollection

    @get:OutputFile
    val stampFile: Provider<RegularFile> = project.layout.buildDirectory.file("tmp/${name}/check.stamp")

    @TaskAction
    protected fun checkPackages() {
        val incompatiblePackages = sourceRoots.files
            .asSequence()
            .flatMap(this::findPackagesWithoutPackageInfoInRoot)
            .toSortedSet(Comparator.comparing { it.second })

        if (incompatiblePackages.isNotEmpty()) {
            throw IllegalStateException(
                incompatiblePackages.joinToString(
                    separator = "\n",
                    prefix = "Packages without package-info.java detected:\n"
                ) {
                    "${it.second} (${it.first.path})"
                }
            )
        } else {
            stampFile.get().touch()
        }
    }

    private fun findPackagesWithoutPackageInfoInRoot(sourceRoot: File): Sequence<Pair<File, String>> {
        if (!sourceRoot.isDirectory) return emptySequence()
        return sourceRoot.walkTopDown()
            .filter { it.isDirectory }
            .mapNotNull { dir ->
                val javaFiles = dir.listFiles { f -> f.isFile && f.extension == "java" }?.toList().orEmpty()

                when {
                    javaFiles.isEmpty() -> null
                    javaFiles.any { it.name == "package-info.java" } -> null
                    else -> dir to dir.relativeTo(sourceRoot).invariantSeparatorsPath.replace('/', '.')
                }
            }
    }

    private fun RegularFile.touch() {
        val filePath = asFile.toPath()
        filePath.deleteIfExists()
        filePath.createFile()
    }
}

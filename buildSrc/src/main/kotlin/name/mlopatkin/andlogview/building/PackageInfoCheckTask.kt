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

    private sealed class PackageError(val path: File, val packageName: String) : Comparable<PackageError> {
        // This format and using logs to output instead of exception is to make sure IDE can convert error messages to
        // clickable links.
        val message = "$path:0: $packageName"

        class NoPackageInfo(path: File, packageName: String) : PackageError(path, packageName)
        class NoAnnotation(path: File, packageName: String) : PackageError(File(path, "package-info.java"), packageName)

        override fun compareTo(other: PackageError): Int {
            return when (val packageCompareResult = packageName.compareTo(other.packageName)) {
                0 -> path.compareTo(other.path)
                else -> packageCompareResult
            }
        }
    }

    @TaskAction
    protected fun checkPackages() {
        val incompatiblePackages = sourceRoots.files
            .asSequence()
            .flatMap(this::findPackagesWithoutPackageInfoInRoot)
            .toSortedSet()

        val failures = buildList {
            formatErrorMessageInto<PackageError.NoPackageInfo>(
                "No package-info.java in packages",
                incompatiblePackages,
                this
            )

            formatErrorMessageInto<PackageError.NoAnnotation>(
                "Missing @NullMarked annotation in packages",
                incompatiblePackages,
                this
            )
        }

        if (failures.isNotEmpty()) {
            val message = failures.joinToString("\n")
            logger.error(message)
            throw RuntimeException("Some package-info.java are not well-formed. Check the logs for the list.")
        }

        stampFile.get().touch()
    }

    private inline fun <reified T : PackageError> formatErrorMessageInto(
        errorMessage: String,
        packageErrors: Set<PackageError>,
        failures: MutableList<String>
    ) {
        val failure = packageErrors
            .filterIsInstance<T>()
            .ifEmpty { null }
            ?.joinToString(
                separator = "\n",
                prefix = "$errorMessage:\n"
            ) {
                it.message
            }
        failure?.let { failures.add(it) }
    }

    private fun findPackagesWithoutPackageInfoInRoot(sourceRoot: File): Sequence<PackageError> {
        if (!sourceRoot.isDirectory) return emptySequence()
        return sourceRoot.walkTopDown()
            .filter { it.isDirectory }
            .mapNotNull { dir ->
                val javaFiles = listJavaSources(dir)

                val packageName = dir.relativeTo(sourceRoot).invariantSeparatorsPath.replace('/', '.')
                when {
                    javaFiles.isEmpty() -> null
                    !hasPackageInfo(javaFiles) -> PackageError.NoPackageInfo(
                        dir,
                        packageName
                    )

                    !isPackageAnnotated(
                        dir,
                        "@NullMarked",
                        "@org.jspecify.annotations"
                    ) -> PackageError.NoAnnotation(dir, packageName)

                    else -> null
                }
            }
    }

    private fun isPackageAnnotated(dir: File, vararg annotation: String): Boolean {
        val text = File(dir, "package-info.java").readText()
        return annotation.any { text.contains(it) }
    }

    private fun listJavaSources(dir: File): List<File> =
        dir.listFiles { f -> f.isFile && f.extension == "java" }?.toList().orEmpty()

    private fun hasPackageInfo(javaFiles: List<File>): Boolean = javaFiles.any { it.name == "package-info.java" }

    private fun RegularFile.touch() {
        val filePath = asFile.toPath()
        filePath.deleteIfExists()
        filePath.createFile()
    }
}

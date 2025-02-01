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

import org.beryx.runtime.RuntimePlugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JavaToolchainSpec

// This is a grab-bag of oneliners

/**
 * Converts a file system location provider to a provider of its path as a String
 */
internal val Provider<out FileSystemLocation>.asPath: Provider<String>
    get() = map { it.asFile.path }

/**
 * A helper to build the command line. If the provider is present, returns a list of switch and the provider's value,
 * otherwise returns an empty list.
 */
internal fun optionalSwitch(switch: String, value: Provider<out String>): List<String> {
    return value.map { listOf(switch, it) }.orElse(listOf()).get()
}

/**
 * A helper to build the command line. If the provider is present, appends the switch and the provider's value,
 * otherwise does nothing.
 */
internal fun MutableList<String>.withOptionalSwitch(switch: String, value: Provider<out String>) {
    addAll(optionalSwitch(switch, value))
}

/**
 * Adds an optional file input of `value` named `name`. The `value` can be an empty provider.
 */
internal fun Task.withOptionalInput(name: String, value: Provider<RegularFile>) {
    inputs.files(value).withPropertyName(name).optional()
}

/**
 * Runs the provided block with the value of the provider if it is present, otherwise does nothing.
 */
internal inline fun <T> Provider<out T>.ifPresent(block: (T) -> Unit) {
    if (isPresent) {
        block(get())
    }
}

/**
 * The shortcut for a provider of the build directory of the project.
 */
internal val Project.theBuildDir
    get() = layout.buildDirectory

/**
 * Returns the provider of path to the JDK installation that matches the given toolchain spec.
 */
internal fun JavaToolchainService.locationOf(spec: JavaToolchainSpec) =
    compilerFor(spec).map { it.metadata.installationPath }

/**
 * A nicer set of constants for [RuntimePlugin].
 */
object RuntimePluginExt {
    val TASK_NAME_JPACKAGE_IMAGE: String = RuntimePlugin.getTASK_NAME_JPACKAGE_IMAGE()
    val TASK_NAME_JPACKAGE: String = RuntimePlugin.getTASK_NAME_JPACKAGE()
}

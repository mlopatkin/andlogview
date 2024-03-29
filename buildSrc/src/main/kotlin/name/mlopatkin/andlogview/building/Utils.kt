/*
 * Copyright 2021 Mikhail Lopatkin
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

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.the
import org.gradle.plugin.use.PluginDependency

// Workaround to expose version catalog to script plugins.
// See https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
val Project.buildLibs: LibrariesForLibs
    get() = the()

val Provider<PluginDependency>.pluginId
    get() = get().pluginId

class JdkVersion(private val provider: Provider<String>) {
    val intProvider
        get() = provider.map { it.toInt() }

    val int
        get() = intProvider.get()

    val string
        get() = provider.get()

    val languageVersion
        get() = provider.map { JavaLanguageVersion.of(it) }
}

val Project.theBuildDir: DirectoryProperty
    get() = layout.buildDirectory

fun Provider<out FileSystemLocation>.toFile() = get().asFile

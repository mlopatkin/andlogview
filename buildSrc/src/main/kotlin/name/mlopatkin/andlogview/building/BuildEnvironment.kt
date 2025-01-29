/*
 * Copyright 2022 the Andlogview authors
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

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.assign
import org.gradle.platform.Architecture
import org.gradle.platform.BuildPlatform
import org.gradle.platform.OperatingSystem
import javax.inject.Inject

/**
 * Helper methods to access environment parameters: is build run by CI server? what revision is checked out?
 */
abstract class BuildEnvironment(project: Project) {

    @get:Inject
    protected abstract val platform: BuildPlatform

    /**
     * Is `true` if the current build should use `-SNAPSHOT` version, `false` if not.
     */
    val isSnapshot: Boolean
        get() {
            return !"false".equals(System.getenv("LOGVIEW_SNAPSHOT_BUILD"), ignoreCase = true)
        }

    /**
     * Provides the source revision from the VCS.
     */
    @Suppress("MemberVisibilityCanBePrivate")  // The warning is false positive
    val sourceRevision: Provider<String>

    init {
        with(project) {
            val isCi = providers.environmentVariable("CI")
            val bitbucketPipelinesRevision = providers.environmentVariable("BITBUCKET_COMMIT")
            val githubActionsRevision = providers.environmentVariable("GITHUB_SHA")

            val gitRevision = providers.of(GitRevisionValueSource::class.java) {
                parameters {
                    repoRoot = projectDir
                    fallback = "n/a"
                }
            }

            sourceRevision = isCi.flatMap { isCiValue ->
                if (isCiValue.toBoolean()) {
                    githubActionsRevision.orElse(bitbucketPipelinesRevision)
                } else {
                    gitRevision
                }
            }.orElse(gitRevision)
        }
    }

    /**
     * Is `true` if building on Linux
     */
    val isLinux
        get() = platform.operatingSystem == OperatingSystem.LINUX

    /**
     * Is `true` if building on macOS
     */
    val isMacos
        get() = platform.operatingSystem == OperatingSystem.MAC_OS

    /**
     * Is `true` if building on Windows
     */
    val isWindows
        get() = platform.operatingSystem == OperatingSystem.WINDOWS

    /**
     * A string representation of the current's machine architecture. Can be `null` if building architecture-specific
     * packages for it isn't supported.
     */
    val architecture
        get() = when (platform.architecture) {
            Architecture.X86 -> "x86"
            Architecture.X86_64 -> "x64"
            Architecture.AARCH64 -> "arm64"
            else -> null
        }

    /**
     * A string representation of the current's machine OS. Can be `null` if building OS-specific packages for it isn't
     * supported.
     */
    val platformName
        get() = when (platform.operatingSystem) {
            OperatingSystem.LINUX -> "linux"
            OperatingSystem.WINDOWS -> "win"
            OperatingSystem.MAC_OS -> "mac"
            else -> null
        }
}

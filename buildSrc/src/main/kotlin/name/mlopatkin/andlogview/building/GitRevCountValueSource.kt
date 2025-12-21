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

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.slf4j.LoggerFactory
import java.io.IOException

abstract class GitRevCountValueSource : ValueSource<Int, GitRevCountValueSource.Params> {
    interface Params : ValueSourceParameters {
        val repoRoot: DirectoryProperty
    }

    override fun obtain(): Int {
        try {
            val repoRoot = parameters.repoRoot.toFile()
            val lastReleaseTag = git(
                repoRoot,
                "describe",
                "--abbrev=0",
                "--tags",
                "nightly^" // Exclude nightly tag
            )
            require(lastReleaseTag.matches("[0-9]+\\.[0-9]+(\\.[0-9]+)?".toRegex())) {
                "Invalid version tag '$lastReleaseTag'"
            }

            val revCountSinceRelease = git(
                repoRoot,
                "rev-list",
                "--count",
                "--first-parent",
                "$lastReleaseTag..HEAD"
            )

            return revCountSinceRelease.toInt()
        } catch (e: IOException) {
            throw RuntimeException("Cannot fetch the revision count: ${e.message}", e)
        }
    }
}

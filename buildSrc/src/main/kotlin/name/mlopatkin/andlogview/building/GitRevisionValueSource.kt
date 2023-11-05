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

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.slf4j.LoggerFactory
import java.io.IOException

private val logger = LoggerFactory.getLogger("Git")

internal abstract class GitRevisionValueSource : ValueSource<String, GitRevisionValueSource.Params> {
    interface Params : ValueSourceParameters {
        val fallback: Property<String>
        val repoRoot: DirectoryProperty
    }

    override fun obtain(): String {
        val command = listOf(
            "git",  // Only try to find Git in PATH
            "describe",
            "--always",  // Always output commit hash
            "--exclude=*",  // Ignore all tags, only hash is sufficient for now
            "--dirty=+"  // Add '+' suffix if the working copy is dirty
        )
        try {
            val gitDescribe = ProcessBuilder(command)
                .directory(parameters.repoRoot.toFile())
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start()

            gitDescribe.use {
                val result = inputStream.bufferedReader().readText().trim()
                if (waitForSuccess()) {
                    return result
                }
            }
        } catch (e: IOException) {
            logger.warn("Failed to get version info from Git", e)
        }
        return parameters.fallback.get()
    }
}

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

import java.io.File
import java.io.IOException


/**
 * Run Git in the specified directory.
 */
fun git(repoRoot: File, vararg args: String): String {
    val command = buildList {
        add("git") // Only try to find Git in PATH
        addAll(args)
    }

    val git = ProcessBuilder(command)
        .directory(repoRoot)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start()

    return git.use {
        val result = inputStream.bufferedReader().readText().trim()
        if (waitForSuccess()) {
            result
        } else {
            throw IOException("Git process failed")
        }
    }
}

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

import java.io.Closeable
import java.io.IOException

/**
 * Helper to close multiple Closeables in a single `use` construct.
 */
fun closer(vararg closeables: Closeable): Closeable {
    return Closeable {
        // Even if any closeable throws an exception, we still attempt to close other ones.
        // All exception are collected and added as "suppressed" to this `pendingException`. The latter is
        // lazily initialized.
        var pendingException: IOException? = null
        closeables.forEach { c ->
            try {
                c.close()
            } catch (th: Throwable) {
                pendingException =
                    (pendingException
                        ?: IOException("Failed to close the resource")).apply { addSuppressed(th) }
            }
        }
        pendingException?.let { throw it }
    }
}

/**
 * Executes block for the given process, waits for the process to exit and closes all process streams afterward.
 */
inline fun <R> Process.use(block: Process.() -> R): R {
    closer(inputStream, outputStream, errorStream).use {
        try {
            return block()
        } finally {
            waitFor()
        }
    }
}

/**
 * Waits for the process to complete and returns `true` if the process completed successfully
 *
 * @see Process.waitFor
 */
fun Process.waitForSuccess(): Boolean {
    return waitFor() == 0
}

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

package name.mlopatkin.gradleplugins.licenses

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.util.zip.ZipFile

sealed interface Resource : Serializable {
    sealed interface BinaryResource : Resource {
        fun loadFromBinary(jarFile: File): String
    }

    sealed interface SourceResource : BinaryResource {
        override fun loadFromBinary(jarFile: File): String = load()
        fun load(): String
    }

    private class JarResource(
        @get:Input  // We cannot get the JAR contents here, but tasks usually depend on it in some other way
        val pathInJar: String
    ) : BinaryResource {
        override fun loadFromBinary(jarFile: File): String {
            ZipFile(jarFile).use { jar ->
                val entry =
                    jar.getEntry(pathInJar) ?: throw IOException("Entry $pathInJar not found in ${jarFile.path}")
                jar.getInputStream(entry).use {
                    return it.reader().readText()
                }
            }
        }
    }

    private class FileResource(
        @get:InputFile
        @get:PathSensitive(PathSensitivity.NONE)
        val file: File
    ) : SourceResource, BinaryResource {
        override fun load(): String {
            return file.readText()
        }
    }

    private class TextResource(
        @get:Input
        val text: String
    ) : SourceResource, BinaryResource {
        override fun load(): String = text
    }

    companion object {
        internal fun ofFile(path: File): SourceResource = FileResource(path)
        internal fun fromJar(pathInJar: String): BinaryResource = JarResource(pathInJar)
        internal fun ofText(text: String): SourceResource = TextResource(text)
    }
}



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

import java.io.File
import java.io.Serializable

sealed interface License : Serializable {
    interface SourceLicense : License {
        fun withNotice(notice: Resource.SourceResource): SourceLicense

        fun buildText(): LicenseText
    }

    interface BinaryLicense : License {
        fun withNotice(notice: Resource): BinaryLicense

        fun buildText(artifact: File): LicenseText
    }

    interface AnyLicense : SourceLicense, BinaryLicense {
        override fun withNotice(notice: Resource.SourceResource): AnyLicense
        override fun withNotice(notice: Resource): BinaryLicense
    }

    class WellKnown(override val name: String, override val spdxId: String) : AnyLicense, LicenseText {
        override fun buildText(artifact: File) = this

        override fun buildText(): LicenseText = this

        override val text: String
            get() = javaClass.getResourceAsStream(spdxId)?.use { it.bufferedReader().use { it.readText() } }
                ?: throw IllegalArgumentException("Can't read text for $spdxId")

        fun fromJar(pathInJar: String): BinaryLicense =
            BinaryResourceBackedLicense(name, spdxId, listOf(Resource.fromJar(pathInJar)))

        fun fromFile(path: File): AnyLicense = SourceResourceBackedLicense(name, spdxId, listOf(Resource.ofFile(path)))

        override fun withNotice(notice: Resource.SourceResource): AnyLicense =
            SourceResourceBackedLicense(name, spdxId, listOf(notice, Resource.ofText(text)))

        override fun withNotice(notice: Resource): BinaryLicense =
            BinaryResourceBackedLicense(name, spdxId, listOf(notice as Resource.BinaryResource, Resource.ofText(text)))
    }

    companion object {
        fun apache2(): WellKnown = WellKnown("Apache License Version 2.0", "Apache-2.0")

        fun mit() = IncompleteLicense("MIT License", "MIT")

        fun bsd3() = IncompleteLicense("BSD 3-Clause License", "BSD-3-Clause")

        fun ccBy4() = WellKnown("CC BY 4.0", "CC-BY-4.0")

        fun publicDomain() = IncompleteLicense("Public Domain", "PD")
    }

    /**
     * Some licenses, like MIT, always have customized text that has to be provided separately.
     */
    class IncompleteLicense(private val name: String, private val spdxId: String) {
        fun fromFile(path: File): AnyLicense = SourceResourceBackedLicense(name, spdxId, listOf(Resource.ofFile(path)))

        fun fromJar(pathInJar: String): BinaryLicense =
            BinaryResourceBackedLicense(name, spdxId, listOf(Resource.fromJar(pathInJar)))
    }

    private class SourceResourceBackedLicense(
        private val name: String,
        private val spdxId: String,
        private val fileResources: List<Resource.SourceResource>
    ) : AnyLicense {
        override fun buildText(): LicenseText {
            val buffer = StringBuilder()
            fileResources.forEach {
                buffer.appendLine(it.load())
            }

            return object : LicenseText {
                override val name: String = this@SourceResourceBackedLicense.name
                override val spdxId: String = this@SourceResourceBackedLicense.spdxId
                override val text: String = buffer.toString()
            }
        }

        override fun withNotice(notice: Resource.SourceResource): AnyLicense {
            return SourceResourceBackedLicense(name, spdxId, fileResources + notice)
        }

        override fun withNotice(notice: Resource): BinaryLicense {
            return when (notice) {
                is Resource.SourceResource -> withNotice(notice)
                is Resource.BinaryResource -> TODO()
            }
        }

        override fun buildText(artifact: File): LicenseText = buildText()
    }

    private class BinaryResourceBackedLicense(
        private val name: String,
        private val spdxId: String,
        private val resources: List<Resource.BinaryResource>
    ) : BinaryLicense {
        override fun withNotice(notice: Resource): BinaryLicense {
            return when (notice) {
                is Resource.BinaryResource -> {
                    BinaryResourceBackedLicense(name, spdxId, resources + notice)
                }
            }
        }

        override fun buildText(artifact: File): LicenseText {
            val buffer = StringBuilder()
            resources.forEach {
                buffer.appendLine(it.loadFromBinary(artifact))
            }

            return object : LicenseText {
                override val name: String = this@BinaryResourceBackedLicense.name
                override val spdxId: String = this@BinaryResourceBackedLicense.spdxId
                override val text: String = buffer.toString()
            }
        }
    }
}

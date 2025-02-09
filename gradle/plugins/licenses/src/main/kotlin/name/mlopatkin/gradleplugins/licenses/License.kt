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

/**
 * A License for the dependency
 */
sealed interface License : Serializable {
    /**
     * A license that can fetch some resources from the runtime JAR of the dependency.
     */
    sealed interface BinaryLicense : License {
        /**
         * Adds a notice from some resource.
         *
         * @see LicenseExtension.fromJar
         * @see LicenseExtension.fromFile
         */
        fun withNotice(notice: Resource): BinaryLicense

        /**
         * Builds the license text
         */
        fun buildText(artifact: File): LicenseText
    }

    /**
     * A license that can be built without looking into the runtime JAR.
     */
    sealed interface SourceLicense : BinaryLicense {
        fun withNotice(notice: Resource.SourceResource): SourceLicense

        fun buildText(): LicenseText
    }

    companion object {
        private class LicenseFactory(val name: String, val spdxId: String) {
            fun sourceLicense(resource: Resource.SourceResource): SourceLicense {
                return SourceResourceBackedLicense(name, spdxId, resource)
            }

            fun binaryLicense(resource: Resource): BinaryLicense {
                return when (resource) {
                    is Resource.SourceResource -> sourceLicense(resource)
                    is Resource.BinaryResource -> BinaryResourceBackedLicense(name, spdxId, resource)
                }
            }
        }

        private val apache2 = LicenseFactory("Apache License Version 2.0", "Apache-2.0")
        private val mit = LicenseFactory("MIT License", "MIT")
        private val bsd3clause = LicenseFactory("BSD 3-Clause License", "BSD-3-Clause")
        private val ccBy4 = LicenseFactory("CC BY 4.0", "CC-BY-4.0")
        private val publicDomain = LicenseFactory("Public Domain", "X-PD")

        /**
         * An Apache License Version 2.0.
         *
         * @param resource optional custom text
         */
        fun apache2(resource: Resource.SourceResource = Resource.ofText(wellKnownText(apache2.spdxId))) =
            apache2.sourceLicense(resource)

        /**
         * An Apache License Version 2.0.
         *
         * @param resource custom text from a JAR
         */
        fun apache2(resource: Resource) = apache2.binaryLicense(resource)

        /**
         * MIT License.
         *
         * @param resource the license text
         */
        fun mit(resource: Resource.SourceResource) = mit.sourceLicense(resource)

        /**
         * MIT License.
         *
         * @param resource the license text
         */
        fun mit(resource: Resource) = mit.binaryLicense(resource)

        /**
         * BSD 3-Clause License.
         *
         * @param resource the license text
         */
        fun bsd3(resource: Resource.SourceResource) = bsd3clause.sourceLicense(resource)

        /**
         * BSD 3-Clause License.
         *
         * @param resource the license text
         */
        fun bsd3(resource: Resource) = bsd3clause.binaryLicense(resource)

        /**
         * Creative Commons Attribution 4.0
         *
         * @param resource the license text
         */
        // TODO(mlopatkin) The text can be optional?
        fun ccBy4(resource: Resource.SourceResource) = ccBy4.sourceLicense(resource)

        /**
         * Creative Commons Attribution 4.0
         *
         * @param resource the license text
         */
        fun ccBy4(resource: Resource) = ccBy4.binaryLicense(resource)

        /**
         * Public Domain
         *
         * @param resource the license text
         */
        fun publicDomain(resource: Resource.SourceResource) = publicDomain.sourceLicense(resource)

        /**
         * Public Domain
         *
         * @param resource the license text
         */
        fun publicDomain(resource: Resource) = publicDomain.binaryLicense(resource)

        private fun wellKnownText(spdxId: String): String {
            return License::class.java.getResourceAsStream(spdxId)?.use {
                it.bufferedReader().use { reader ->
                    reader.readText()
                }
            } ?: throw IllegalArgumentException("Can't read text for $spdxId")
        }
    }

    private class SourceResourceBackedLicense(
        private val name: String,
        private val spdxId: String,
        private val license: Resource.SourceResource,
        private val fileResources: List<Resource.SourceResource> = listOf()
    ) : SourceLicense {
        override fun buildText(): LicenseText {
            val buffer = StringBuilder()
            fileResources.forEach {
                buffer.appendLine(it.load())
            }

            buffer.appendLine(license.load())

            return LicenseText(name, spdxId, buffer.toString())
        }

        override fun withNotice(notice: Resource.SourceResource): SourceLicense {
            return SourceResourceBackedLicense(name, spdxId, license, fileResources + notice)
        }

        override fun withNotice(notice: Resource): BinaryLicense {
            return when (notice) {
                is Resource.SourceResource -> withNotice(notice)
                is Resource.BinaryResource -> BinaryResourceBackedLicense(name, spdxId, license, fileResources + notice)
            }
        }

        override fun buildText(artifact: File): LicenseText = buildText()
    }

    private class BinaryResourceBackedLicense(
        private val name: String,
        private val spdxId: String,
        private val license: Resource.BinaryResource,
        private val resources: List<Resource.BinaryResource> = listOf()
    ) : BinaryLicense {
        override fun withNotice(notice: Resource): BinaryLicense {
            return when (notice) {
                is Resource.BinaryResource -> {
                    BinaryResourceBackedLicense(name, spdxId, license, resources + notice)
                }
            }
        }

        override fun buildText(artifact: File): LicenseText {
            val buffer = StringBuilder()
            resources.forEach {
                buffer.appendLine(it.loadFromBinary(artifact))
            }

            buffer.appendLine(license.loadFromBinary(artifact))

            return LicenseText(name, spdxId, buffer.toString())
        }
    }
}

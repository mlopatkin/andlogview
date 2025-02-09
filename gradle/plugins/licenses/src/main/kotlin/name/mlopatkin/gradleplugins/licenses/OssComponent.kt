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

import com.google.gson.JsonObject
import name.mlopatkin.gradleplugins.licenses.LicensedComponent.LicensedModule
import name.mlopatkin.gradleplugins.licenses.LicensedComponent.LicensedSource
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import java.net.URI
import java.util.Objects

/**
 * The combined text of the license with all notices.
 */
class LicenseText(
    val name: String,
    val spdxId: String,
    val text: String,
)

internal fun Appendable.appendNoticeHeader(noticeScope: String) {
    appendLine(
        """
========================================================================================================================
NOTICE for $noticeScope
------------------------------------------------------------------------------------------------------------------------
""".trimIndent()
    )
}

internal class OssComponent(
    val displayName: String,
    val version: String,
    val homepage: URI,
    val scope: String,
    val licenseText: LicenseText,
) {
    fun appendToNotice(noticeFile: Appendable) {
        noticeFile.appendNoticeHeader("$displayName ($scope)")
        noticeFile.append(licenseText.text)
    }

    fun appendToNoticeScope(noticeFile: Appendable) {
        val subHeader = "License terms for $displayName"
        val underline = "-".repeat(subHeader.length)

        noticeFile.appendLine(subHeader).appendLine(underline).appendLine()
        noticeFile.append(licenseText.text)
    }

    fun toJson(id: Int): JsonObject = jsonObject {
        "id" jsonTo id
        "name" jsonTo displayName
        "version" jsonTo version
        "homepage" jsonTo homepage.toASCIIString()
        "scope" jsonTo scope
        "license" jsonTo licenseText.name
        "spdx" jsonTo licenseText.spdxId
        "licenseText" jsonTo licenseText.text
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is OssComponent) {
            return displayName == other.displayName && scope == other.scope
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(displayName, scope)
    }
}

internal fun buildOssComponents(
    artifacts: Collection<ResolvedArtifactResult>,
    licensedComponents: Collection<LicensedComponent>
): Set<OssComponent> {
    val result = OssComponentsBuildResult(artifacts, licensedComponents)

    require(result.isValid) {
        buildString {
            with(result) {
                if (artifactsWithoutLicense.isNotEmpty()) {
                    appendLine("The following modules do not have license specified:")
                    artifactsWithoutLicense.forEach {
                        appendLine("  $it")
                    }
                }
                if (unusedComponents.isNotEmpty()) {
                    appendLine("The following modules are no longer used:")
                    unusedComponents.forEach {
                        appendLine("  $it")
                    }
                }
            }
        }
    }

    return result.components
}

private class OssComponentsBuildResult(
    artifacts: Collection<ResolvedArtifactResult>,
    licensedComponents: Collection<LicensedComponent>
) {
    val artifactsWithoutLicense: Set<ModuleKey>
    val unusedComponents: Set<ModuleKey>

    val components: Set<OssComponent>

    val isValid
        get() = artifactsWithoutLicense.isEmpty() && unusedComponents.isEmpty()

    init {
        val licensedModules = licensedComponents.filterIsInstance<LicensedModule>()
        val licensedSources = licensedComponents.filterIsInstance<LicensedSource>()

        require(licensedComponents.size == licensedModules.size + licensedSources.size) {
            "Unexpected license type found"
        }

        val moduleArtifacts: Map<ModuleKey, ResolvedArtifactResult> = buildMap {
            artifacts.forEach {
                when (val id = it.componentId()) {
                    is ModuleComponentIdentifier -> put(ModuleKey(id.group, id.module), it)
                }
            }
        }

        val moduleLicenses: Map<ModuleKey, LicensedModule> = buildMap {
            licensedModules.forEach {
                put(it.getKey(), it)
            }
        }


        artifactsWithoutLicense = moduleArtifacts.keys subtract moduleLicenses.keys
        unusedComponents = moduleLicenses.keys subtract moduleArtifacts.keys

        val result = zipMaps(moduleArtifacts, moduleLicenses) { artifact, data ->
            OssComponent(
                data.name,
                (artifact.componentId() as ModuleComponentIdentifier).version,
                data.homepage,
                artifact.componentId().toString(),
                data.license.buildText(artifact.file)
            )
        }

        licensedSources.forEach {
            result.add(OssComponent(it.name, "n/a", it.homepage, "file: ${it.scope}", it.license.buildText()))
        }

        components = HashSet(result)
    }
}


internal fun ResolvedArtifactResult.componentId() = id.componentIdentifier

private data class ModuleKey(val group: String, val module: String) {
    override fun toString(): String {
        return "$group:$module"
    }
}

private fun ResolvedArtifactResult.getKey(): ModuleKey? = when (val id = this.componentId()) {
    is ModuleComponentIdentifier -> ModuleKey(id.group, id.module)
    else -> null
}

private fun LicensedComponent.getKey(): ModuleKey? = when (this) {
    is LicensedModule -> getKey()
    else -> null
}

private fun LicensedModule.getKey(): ModuleKey = ModuleKey(group, module)

private fun <K, V1, V2, R> zipMaps(a: Map<K, V1>, b: Map<K, V2>, combiner: (V1, V2) -> R): MutableList<R> {
    val result = ArrayList<R>(a.size)

    a.forEach { k, v1 ->
        b[k]?.let { v2 -> result.add(combiner(v1, v2)) }
    }

    return result
}

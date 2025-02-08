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

import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import java.net.URI
import javax.inject.Inject

internal abstract class LicenseExtensionImpl : LicenseExtension {
    @get:Inject
    abstract val layout: ProjectLayout

    val bundledDependencies: Provider<Set<ResolvedArtifactResult>>
        get() = configuration.flatMap { it.incoming.artifacts.resolvedArtifacts }

    abstract val dependencies: ListProperty<LicensedComponent>

    override fun binaryDependency(
        group: String,
        name: String,
        displayName: String,
        homepage: URI,
        license: License.BinaryLicense
    ) {
        dependencies.add(
            LicensedComponent.LicensedModule(group, name, displayName, homepage, license)
        )
    }

    override fun sourceDependency(
        scope: String,
        gav: String,
        displayName: String,
        homepage: URI,
        license: License.SourceLicense
    ) {
        dependencies.add(
            LicensedComponent.LicensedSource(displayName, homepage, license, scope)
        )
    }

    override fun fromFile(path: String): Resource.SourceResource {
        return Resource.ofFile(layout.projectDirectory.file(path).asFile)
    }

    override fun fromJar(path: String): Resource.BinaryResource {
        return Resource.fromJar(path)
    }
}

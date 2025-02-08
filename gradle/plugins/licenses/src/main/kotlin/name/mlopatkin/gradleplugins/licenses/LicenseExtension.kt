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

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import java.net.URI

interface LicenseExtension {
    val configuration: Property<Configuration>

    fun binaryDependency(
        group: String,
        name: String,
        displayName: String,
        homepage: URI,
        license: License.BinaryLicense
    )

    fun binaryDependency(module: String, displayName: String, homepage: URI, license: License.BinaryLicense) {
        val (group, name) = module.split(":")
        return binaryDependency(group, name, displayName, homepage, license)
    }

    fun binaryDependency(
        module: Provider<out MinimalExternalModuleDependency>,
        displayName: String,
        homepage: URI,
        license: License.BinaryLicense
    ) {
        with(module.get()) {
            return binaryDependency(group, name, displayName, homepage, license)
        }
    }

    fun sourceDependency(scope: String, gav: String, displayName: String, homepage: URI, license: License.SourceLicense)

    fun fromFile(path: String): Resource.SourceResource
}


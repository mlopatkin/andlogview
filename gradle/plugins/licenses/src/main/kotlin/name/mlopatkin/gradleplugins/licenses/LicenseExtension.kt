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

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import java.net.URI

/**
 * Primary configuration of the plugin. Available with `licenses` extension on the Project.
 */
interface LicenseExtension {
    /**
     * The configuration with shipped dependencies, typically the runtime classpath.
     */
    val configuration: Property<Configuration>

    /**
     * Defines the metadata for a binary dependency resolved as an artifact.
     *
     * @param group the group of the dependency
     * @param name the name of the dependency
     * @param displayName the user-visible name of the dependency
     * @param homepage the home page of the dependency
     * @param license the license of the dependency. Can refer to resources in the dependencies' JAR file
     */
    fun binaryDependency(
        group: String,
        name: String,
        displayName: String,
        homepage: URI,
        license: License.BinaryLicense
    )

    /**
     * Defines the metadata for a binary dependency resolved as an artifact.
     *
     * @param module the `group:name` id of the dependency. The version is optional and is ignored if provided.
     * @param displayName the user-visible name of the dependency
     * @param homepage the home page of the dependency
     * @param license the license of the dependency. Can refer to resources in the dependencies' JAR file
     */
    fun binaryDependency(module: String, displayName: String, homepage: URI, license: License.BinaryLicense) {
        val (group, name) = module.split(":")
        return binaryDependency(group, name, displayName, homepage, license)
    }

    /**
     * Defines the metadata for a binary dependency resolved as an artifact.
     *
     * @param module the version catalog entry for the dependency. The version is optional and is ignored if available.
     * @param displayName the user-visible name of the dependency
     * @param homepage the home page of the dependency
     * @param license the license of the dependency. Can refer to resources in the dependencies' JAR file
     */
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

    /**
     * Defines the metadata for a dependency that exists as sources.
     *
     * @param scope the resources that this dependency provides (e.g. source files)
     * @param gav the GAV coordinates of the original artifact from which this dependency was taken from
     * @param displayName the user-visible name of the dependency
     * @param homepage the home page of the dependency
     * @param license the license of the dependency. Cannot refer to resource from JAR because there is no JAR
     */
    fun sourceDependency(scope: String, gav: String, displayName: String, homepage: URI, license: License.SourceLicense)

    /**
     * A resource that will be loaded from the given file. The path is resolved according to [Project.file].
     */
    fun fromFile(path: String): Resource.SourceResource

    /**
     * A resource that will be extracted from the artifact's JAR.
     */
    fun fromJar(path: String): Resource.BinaryResource
}


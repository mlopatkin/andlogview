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
import org.gradle.api.tasks.Nested
import java.io.Serializable
import java.net.URI

sealed interface LicensedComponent : Serializable {
    @get:Input
    val name: String
    @get:Input
    val homepage: URI
    @get:Nested
    val license: License

    data class LicensedSource(
        override val name: String,
        override val homepage: URI,
        override val license: License.SourceLicense,
        @get:Input
        val scope: String,
    ) : LicensedComponent

    data class LicensedModule(
        @get:Input
        val group: String,
        @get:Input
        val module: String,
        override val name: String,
        override val homepage: URI,
        override val license: License.BinaryLicense,
    ) : LicensedComponent
}

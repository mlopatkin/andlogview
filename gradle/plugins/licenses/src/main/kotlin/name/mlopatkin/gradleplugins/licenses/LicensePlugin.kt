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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class LicensePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.applyThisPlugin()
    }

    private fun Project.applyThisPlugin() {
        val licenses =
            extensions.create(LicenseExtension::class, "licenses", LicenseExtensionImpl::class) as LicenseExtensionImpl

        tasks.register<GenerateLicensesListJson>("generateLicenses") {
            bundledDependencies = licenses.bundledDependencies
            licensedComponentList = licenses.dependencies
            outputJsonFile = layout.buildDirectory.file("generated/licenses/licenses.json")
        }

        tasks.register<GenerateNotices>("generateNotices") {
            bundledDependencies = licenses.bundledDependencies
            licensedComponentList = licenses.dependencies
            noticeOutputFile = layout.buildDirectory.file("generated/licenses/NOTICE")
        }
    }
}

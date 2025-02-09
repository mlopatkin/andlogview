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
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class GenerateLicensesListJson : BaseLicenseTask() {
    @get:OutputFile
    abstract val outputJsonFile: RegularFileProperty

    @TaskAction
    fun action() {
        actionImpl(bundledDependencies.get(), licensedComponentList.get(), outputJsonFile.get())
    }

    private fun actionImpl(
        bundledDependencies: Set<ResolvedArtifactResult>,
        licensedComponents: List<LicensedComponent>,
        outputJsonFile: RegularFile
    ) {
        val components = buildOssComponents(bundledDependencies, licensedComponents)

        json(outputJsonFile.asFile) {
            jsonArray {
                components.forEachIndexed { id, component ->
                    add(component.toJson(id))
                }
            }
        }
    }
}

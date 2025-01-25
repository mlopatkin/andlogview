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

package name.mlopatkin.gradleplugins.freemarker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType

@Suppress("unused")
abstract class FreemarkerPlugin : Plugin<Project> {
    private val freemarkerConfigurationName = "freemarker"
    private val freemarkerRuntimeConfigurationName = "freemarkerDefaultRuntime"

    override fun apply(project: Project) {
        val freemarkerConfiguration = project.configurations.run {
            dependencyScope(freemarkerConfigurationName) {
                defaultDependencies {
                    add(project.dependencies.create("org.freemarker:freemarker:2.3.34"))
                }
            }
        }
        val freemarkerRuntimeConfiguration = project.configurations.run {
            resolvable(freemarkerRuntimeConfigurationName) {
                extendsFrom(freemarkerConfiguration.get())
            }
        }

        project.tasks.withType<FreeMarkerTask> {
            freeMarkerClasspath.from(freemarkerRuntimeConfiguration)
        }
    }
}

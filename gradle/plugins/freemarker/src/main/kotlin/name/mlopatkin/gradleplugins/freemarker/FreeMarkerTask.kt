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

import freemarker.template.Configuration
import freemarker.template.Template
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

abstract class FreeMarkerTask : DefaultTask() {
    @get:Classpath
    abstract val freeMarkerClasspath: ConfigurableFileCollection

    @get:Input
    abstract val definitions: MapProperty<String, String>

    @get:InputDirectory
    @get:Optional
    abstract val includes: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val templates: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    protected abstract val worker: WorkerExecutor

    @TaskAction
    fun action() {
        val queue = worker.classLoaderIsolation {
            classpath.from(freeMarkerClasspath)
        }

        templates.files.forEach { inputTemplate ->
            queue.submit(FreeMarkerWorkItem::class) {
                template.set(inputTemplate)
                includes.set(this@FreeMarkerTask.includes)
                definitions.set(this@FreeMarkerTask.definitions)
                outputDirectory.set(this@FreeMarkerTask.outputDirectory)
            }
        }
    }
}

interface FreeMarkerWorkParams : WorkParameters {
    val template: RegularFileProperty
    val includes: DirectoryProperty
    val definitions: MapProperty<String, String>
    val outputDirectory: DirectoryProperty
}

abstract class FreeMarkerWorkItem : WorkAction<FreeMarkerWorkParams> {
    override fun execute() {
        val cfg = Configuration(Configuration.VERSION_2_3_34).apply {
            defaultEncoding = "UTF-8"
            interpolationSyntax = Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX
            if (parameters.includes.isPresent) {
                setDirectoryForTemplateLoading(parameters.includes.get().asFile)
            }
        }

        with(parameters) {
            val templateFile = template.get().asFile
            val outputFileName = templateFile.nameWithoutExtension
            processTemplate(cfg, templateFile, outputDirectory.file(outputFileName).get().asFile, definitions.get())
        }
    }

    private fun processTemplate(
        cfg: Configuration,
        templateFile: File,
        outputFile: File,
        variables: Map<String, String>
    ) {
        try {
            val template = Template(templateFile.name, templateFile.reader(), cfg)
            outputFile.writer().use { out ->
                template.process(variables, out)
            }
        } catch (e: Exception) {
            val decoratedException = object : RuntimeException(e.message) {
                override fun fillInStackTrace(): Throwable {
                    return this
                }
            }

            decoratedException.stackTrace = e.stackTrace

            throw decoratedException
        }
    }
}

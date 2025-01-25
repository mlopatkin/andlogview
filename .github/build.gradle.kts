import name.mlopatkin.gradleplugins.freemarker.FreeMarkerTask
import java.io.StringReader
import java.util.Properties

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

plugins {
    base
    id("name.mlopatkin.gradleplugins.freemarker")
}


val buildWorkflows = tasks.register<FreeMarkerTask>("buildWorkflows") {
    includes = file("workflow-templates/includes")

    templates = fileTree(file("workflow-templates")) {
        include("*.ftl")
    }
    outputDirectory = layout.buildDirectory.dir("generated/$name")

    val fileContents = providers.fileContents(layout.projectDirectory.file("versions.properties")).asText.map {
        val props = Properties()
        props.load(StringReader(it))

        return@map props.entries.associate { (k, v) -> k.toString() to v.toString() }.toSortedMap()
    }

    definitions = fileContents
}


abstract class CheckWorkflows : DefaultTask() {
    @get:InputFiles
    abstract val generatedWorkflows: ConfigurableFileTree

    @get:InputFiles
    abstract val commitedWorkflows: ConfigurableFileTree

    @TaskAction
    fun action() {
        val commitedWorkflowFiles = commitedWorkflows.files.map { it.relativeTo(commitedWorkflows.dir) }
        val generatedWorkflowFiles = generatedWorkflows.files

        generatedWorkflowFiles.forEach { generatedWorkflow ->
            val relPath = generatedWorkflow.relativeTo(generatedWorkflows.dir)
            if (relPath !in commitedWorkflowFiles) {
                fail(generatedWorkflow, "No commited file for generated workflow")
            }
            if (generatedWorkflow.readText() != File(commitedWorkflows.dir, relPath.path).readText()) {
                fail(generatedWorkflow, "The text of the commited workflow differs")
            }
        }
    }

    private fun fail(generatedFile: File, message: String) {
        throw RuntimeException("${generatedFile.relativeTo(generatedWorkflows.dir).path}: $message")
    }
}


val checkWorkflows = tasks.register<CheckWorkflows>("checkWorkflows") {
    dependsOn(buildWorkflows)  // Why don't I have a dependency through the flatmap?
    generatedWorkflows.run {
        from(buildWorkflows.flatMap { it.outputDirectory })
        include("**/*.yaml")
    }
    commitedWorkflows.run {
        from(file("workflows"))
        include("**/*.yaml")
    }
}

tasks.register<Sync>("commitWorkflows") {
    from(buildWorkflows.flatMap { it.outputDirectory })
    into(file("workflows/"))
}

tasks.check {
    dependsOn(checkWorkflows)
}

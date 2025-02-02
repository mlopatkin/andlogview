/*
 * Copyright 2023 the Andlogview authors
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

package name.mlopatkin.andlogview.building

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

plugins {
    java
}

interface BuildMetadataExtension {
    val revision: Property<String>
    val packageName: Property<String>
    val version: Property<String>
}


val metadataExtension = extensions.create<BuildMetadataExtension>("buildMetadata")

abstract class BuildDateValueSource : ValueSource<String, ValueSourceParameters.None> {
    override fun obtain(): String {
        val buildTime = Instant.now().truncatedTo(ChronoUnit.MINUTES)
        return DateTimeFormatter.ISO_INSTANT.format(buildTime)
    }
}

val generateBuildMetadata by tasks.registering(GenerateBuildMetadata::class) {
    revision = metadataExtension.revision
    packageName = metadataExtension.packageName
    propertyFile = "build-info.properties"
    version = metadataExtension.version
    buildTimestamp = providers.of(BuildDateValueSource::class) {}
    // Like annotationProcessor. Note java/main instead of Gradle's usual main/java.
    into = layout.buildDirectory.dir("generated/sources/metadata/java/main")
}

val metadata by sourceSets.creating {
    java {
        resources.srcDir(generateBuildMetadata)
    }
}

dependencies {
    implementation(metadata.output)
}

normalization {
    runtimeClasspath {
        ignore("**/build-info.properties")
    }
}

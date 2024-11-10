/*
 * Copyright 2024 the Andlogview authors
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

// Make distribution archives reproducible
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    // Set up consistent permissions on files. This is consistent with the permissions on Windows machines and the
    // Docker image.
    dirPermissions {
        this.unix("755")
    }

    filePermissions {
        // We keep the execute bit as is, so the executable scripts remain executable.
        user.apply {
            read = true
            write = true
        }
        listOf(group, other).forEach {
            it.apply {
                read = true
                write = false
            }
        }
    }
}

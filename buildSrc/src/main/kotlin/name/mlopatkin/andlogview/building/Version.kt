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

package name.mlopatkin.andlogview.building

data class Version(val major: Int, val minor: Int, val patch: Int) {

    override fun toString(): String {
        if (patch > 0) {
            return "$major.$minor.$patch"
        }
        return "$major.$minor"
    }

    fun format(pattern: String): String {
        return String.format(pattern, major, minor, patch)
    }

    companion object {
        fun fromString(version: String): Version {
            val pattern = "([0-9]+)\\.([0-9]+)(?:\\.([0-9]+))?".toRegex()

            return when (val match = pattern.matchEntire(version)) {
                null -> throw IllegalArgumentException("Invalid version string $version")
                else -> Version(match[1]!!, match[2]!!, match[3] ?: 0)
            }
        }

        private operator fun MatchResult.get(i: Int): Int? = groups[i]?.value?.toInt()
    }
}

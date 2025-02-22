/*
 * Copyright 2022 the Andlogview authors
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
    id("name.mlopatkin.andlogview.building.java-library-conventions")
}

dependencies {
    api(project(":logmodel"))
    implementation(project(":base"))

    testFixturesApi(platform(libs.test.junit5.bom))
    testFixturesApi(libs.test.junit5.jupiter)

    testFixturesImplementation(project(":base"))
    testFixturesImplementation(testFixtures(project(":logmodel")))
    testFixturesImplementation(libs.gson)

    testImplementation(project(":device"))
    testImplementation(testFixtures(project(":logmodel")))
    testImplementation(libs.gson)
}

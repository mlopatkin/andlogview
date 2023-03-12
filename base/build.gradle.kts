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

    alias(libs.plugins.jmh)

    `java-test-fixtures`
}

description = "Common utilities that every other model can use"

dependencies {
    api(libs.dagger.runtime)

    implementation(libs.guava)
    implementation(libs.log4j)

    testFixturesApi(libs.test.hamcrest.hamcrest)
    testFixturesImplementation(libs.guava)
    testFixturesImplementation(libs.test.mockito.core)
}

sourceSets {
    main {
        java {
            srcDir(file("third-party/observerList/src"))
            srcDir(file("third-party/systemUtils/src"))
        }
    }
}

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

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

/**
 * Extra options to configure FreeMarker.
 */
interface FreemarkerConfiguration {
    /**
     * Variable interpolation syntax.
     */
    enum class InterpolationSyntax {
        /**
         * `${expression}` and the deprecated `#{expression; numFormat}`.
         */
        LEGACY,

        /**
         * `[=expression]` instead of `${expression}`. It does not change `<#if x>` to `[#if x]`;
         * that's square bracket *tag* syntax.
         *
         */
        SQUARE_BRACKET,

        /**
         * `${expression}` only (not `#{expression; numFormat}`).
         *
         * This is the default for the plugin.
         */
        DOLLAR
    }

    /**
     * Variable interpolation syntax. [InterpolationSyntax.DOLLAR] is used by default.
     */
    @get:Input
    val interpolationSyntax: Property<InterpolationSyntax>
}

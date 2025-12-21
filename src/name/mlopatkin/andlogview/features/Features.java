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

package name.mlopatkin.andlogview.features;

import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Collection of the feature flags.
 */
@Singleton
public class Features {
    public final Feature useFilterTree;

    @Inject
    @VisibleForTesting
    public Features() {
        useFilterTree = Feature.create("name.mlopatkin.andlogview.features.useFilterTree", true);
    }
}

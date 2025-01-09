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

package name.mlopatkin.andlogview.widgets;

import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;

/**
 * Helper functions for MigLayout constraints.
 */
public final class MigConstraints {
    private MigConstraints() {}

    /**
     * Returns new Layout Constraints instance.
     *
     * @return the new layout constraints
     */
    public static LC LC() { // NO CHECKSTYLE
        return new LC();
    }

    /**
     * Returns new Row/Column Constraints instance.
     *
     * @return the new row/column constraints
     */
    public static AC AC() { // NO CHECKSTYLE
        return new AC();
    }

    /**
     * Returns new Component Constraints instance.
     *
     * @return the new component constraints.
     */
    public static CC CC() { // NO CHECKSTYLE
        return new CC();
    }
}

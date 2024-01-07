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

package name.mlopatkin.andlogview.filters;

import name.mlopatkin.andlogview.logmodel.LogRecord;

import java.util.function.Predicate;

/**
 * A filter for the log.
 */
public interface Filter extends Predicate<LogRecord> {
    /**
     * The filtering mode used by this filter.
     *
     * @return the mode
     */
    FilteringMode getMode();

    /**
     * Returns true if this filter is enabled and therefore must affect the output.
     *
     * @return true if the filter is enabled
     */
    boolean isEnabled();

    /**
     * Creates an enabled copy of this filter (or this filter if it is already enabled).
     *
     * @return the enabled filter that matches the same lines
     * @implSpec implementors should override this method with an appropriate return type
     */
    Filter enabled();

    /**
     * Creates a disable copy of this filter (or this filter if it is already disabled).
     *
     * @return the disabled filter that matches the same lines
     * @implSpec implementors should override this method with an appropriate return type
     */
    Filter disabled();
}

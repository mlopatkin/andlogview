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

import java.util.Objects;
import java.util.function.Predicate;

public abstract class AbstractToggleFilter<T extends AbstractToggleFilter<T>> extends AbstractFilter<T>
        implements PredicateFilter {
    protected final Predicate<? super LogRecord> predicate;

    public AbstractToggleFilter(FilteringMode mode, boolean enabled, Predicate<? super LogRecord> predicate) {
        super(mode, enabled);
        this.predicate = predicate;
    }

    @Override
    public boolean test(LogRecord logRecord) {
        return predicate.test(logRecord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, predicate);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this
                || (obj instanceof AbstractToggleFilter<?> filter && mode.equals(filter.mode)
                && isEnabled() == filter.isEnabled() && predicate.equals(filter.predicate));
    }
}

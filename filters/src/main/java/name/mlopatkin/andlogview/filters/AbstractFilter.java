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

/**
 * Abstract base class to implement a filter
 *
 * @param <SELF> the implementation type
 */
public abstract class AbstractFilter<SELF extends AbstractFilter<SELF>> implements Filter {
    protected final FilteringMode mode;
    private final boolean enabled;

    protected AbstractFilter(FilteringMode mode, boolean enabled) {
        this.mode = mode;
        this.enabled = enabled;
    }

    @Override
    public final FilteringMode getMode() {
        return mode;
    }

    @SuppressWarnings("unchecked")
    protected final SELF self() {
        return (SELF) this;
    }

    protected abstract SELF copy(boolean enabled);

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public final SELF enabled() {
        return isEnabled() ? self() : copy(true);
    }

    @Override
    public final SELF disabled() {
        return isEnabled() ? copy(false) : self();
    }
}

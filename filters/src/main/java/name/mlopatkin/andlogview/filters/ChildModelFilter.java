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
 * A Filter that has a submodel.
 */
public interface ChildModelFilter extends Filter {
    // TODO {@link FilterModel} should know about these filters and may provide a "parent" model for it.

    /**
     * Returns the child model of this filter. It doesn't include the filters of the parent model.
     *
     * @return the model of this filter
     */
    FilterModel getChildren();
    // TODO(mlopatkin) FilterModel can subscribe to child model upon adding a filter and unsubscribe afterwards.
    //  This way, the notifications can still be sent from the main model, so clients do not have to care about watching
    //  for children and subscribing themselves.
    // TODO(mlopatkin) The returned model doesn't have to be mutable, but some filters may implement it as such.
}

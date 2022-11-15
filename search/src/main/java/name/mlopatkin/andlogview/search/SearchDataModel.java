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

package name.mlopatkin.andlogview.search;

/**
 * A model that provides searchable data and their positions. A position can be used to look up data item in the model
 * or associated structures.
 *
 * @param <T> the type of data
 * @param <P> the type of position
 */
public interface SearchDataModel<T, P> {
    /**
     * Creates a new cursor that points to the beginning of the model.
     *
     * @return the new cursor
     */
    SearchCursor<T, P> newCursor();
}

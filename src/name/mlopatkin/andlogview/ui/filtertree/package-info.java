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

/**
 * The Filter tree. The filters available for the current window form a tree. Simple show/hide/highlight filters are
 * leaf nodes. Window filters can have child filters of their own.
 * <p>
 * The tree uses {@link name.mlopatkin.andlogview.ui.filtertree.FilterTreeModel} to modify and follow changes of the
 * filter set. Results of any modifications are delivered through registered observers.
 * <p>
 * Individual filters are represented as {@link name.mlopatkin.andlogview.ui.filtertree.FilterNodeViewModel}.
 */
@NullMarked
package name.mlopatkin.andlogview.ui.filtertree;

import org.jspecify.annotations.NullMarked;

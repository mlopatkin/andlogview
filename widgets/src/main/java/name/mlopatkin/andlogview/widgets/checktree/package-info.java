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
 * A support classes for {@link javax.swing.JTree} with items that can be checked/unchecked. To use it, you need to:
 * <ul>
 *     <li>Install {@link name.mlopatkin.andlogview.widgets.checktree.CheckFlatTreeUi} for your JTree;</li>
 *     <li>Use {@link name.mlopatkin.andlogview.widgets.checktree.CheckableTreeModel} as the tree model;</li>
 *     <li>Make your {@link javax.swing.tree.TreeCellRenderer} return components implementing
 *     {@link name.mlopatkin.andlogview.widgets.checktree.Checkable} for the items you want to be toggleable.</li>
 * </ul>
 * <p>
 * Note that there is no built-in GUI. Your cell renderer must take care of presenting checked/unchecked status and give
 * the user a way to toggle. For example, you can extend {@link javax.swing.JCheckBox}.
 * </p>
 * <p>
 * Currently it only works with FlatLaF.
 * </p>
 */
@NullMarked
package name.mlopatkin.andlogview.widgets.checktree;

import org.jspecify.annotations.NullMarked;

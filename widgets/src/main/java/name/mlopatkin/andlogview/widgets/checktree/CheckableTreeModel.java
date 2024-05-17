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

package name.mlopatkin.andlogview.widgets.checktree;

import javax.swing.tree.TreeModel;

/**
 * A specialized tree model that can have its nodes checked or unchecked. Not all nodes have to be checkable.
 */
public interface CheckableTreeModel extends TreeModel {
    /**
     * Called to determine if the node can be checked/unchecked.
     *
     * @param node the node
     * @return {@code true} if the node is checkable, or {@code false} if the node is not checkable or doesn't belong to
     *         the model
     */
    boolean isNodeCheckable(Object node);

    /**
     * Called to determine if the node is currently checked. The node is first tested with
     * {@link #isNodeCheckable(Object)}.
     *
     * @param node the node
     * @return {@code true} if the node is checked, {@code false} otherwise
     * @throws IllegalArgumentException if the node doesn't belong to the model or is not checkable
     */
    boolean isNodeChecked(Object node);

    /**
     * Sets the node to be checked/unchecked. Changing the state of the node triggers tree change event.
     *
     * @param node the node to change state
     * @param checked the new state
     * @throws IllegalArgumentException if the node doesn't belong to the model or is not checkable
     */
    void setNodeChecked(Object node, boolean checked);
}

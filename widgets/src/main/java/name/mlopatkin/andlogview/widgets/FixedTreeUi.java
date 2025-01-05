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

import com.formdev.flatlaf.ui.FlatTreeUI;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

/**
 * A FlatLaF-based Tree UI that works around Swing issues.
 */
public class FixedTreeUi extends FlatTreeUI {
    @Override
    protected TreeModelListener createTreeModelListener() {
        var parentListener = super.createTreeModelListener();
        return new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                parentListener.treeNodesChanged(e);
                var parent = e.getTreePath();

                // For some reason, when a direct child of root changes its height, the JTree doesn't recalculate the Y
                // positions of nodes below. We invalidate the heights manually here.
                for (var child : e.getChildren()) {
                    treeState.invalidatePathBounds(parent.pathByAddingChild(child));
                }
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                parentListener.treeNodesInserted(e);
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                parentListener.treeNodesRemoved(e);
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                parentListener.treeStructureChanged(e);
            }
        };
    }

}

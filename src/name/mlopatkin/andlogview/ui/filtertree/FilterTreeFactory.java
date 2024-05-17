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

package name.mlopatkin.andlogview.ui.filtertree;

import name.mlopatkin.andlogview.widgets.checktree.CheckFlatTreeUi;

import javax.inject.Inject;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeSelectionModel;

/**
 * Constructs {@link JTree} that shows the available filters.
 */
public class FilterTreeFactory {
    private final FilterNodeRenderer nodeRenderer;
    private final TreeModelAdapter treeModel;

    @Inject
    FilterTreeFactory(
            FilterNodeRenderer nodeRenderer,
            TreeModelAdapter treeModel) {
        this.nodeRenderer = nodeRenderer;
        this.treeModel = treeModel;
    }

    public JTree buildFilterTree() {
        var filterTree = new JTree(treeModel);

        filterTree.setUI(new CheckFlatTreeUi());

        filterTree.setRootVisible(false);
        filterTree.setShowsRootHandles(false);
        filterTree.setCellRenderer(nodeRenderer);
        filterTree.setSelectionModel(new DefaultTreeSelectionModel());

        return filterTree;
    }
}

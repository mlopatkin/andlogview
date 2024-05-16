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

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeSelectionModel;

/**
 * Constructs {@link JTree} that shows the available filters.
 */
public class FilterTreeFactory {
    private final FilterNodeRenderer nodeRenderer;
    private final TreeModelAdapter treeModel;
    private final Provider<FilterNodeEditor> editorProvider;

    @Inject
    FilterTreeFactory(
            FilterNodeRenderer nodeRenderer,
            TreeModelAdapter treeModel,
            Provider<FilterNodeEditor> editorProvider) {
        this.nodeRenderer = nodeRenderer;
        this.treeModel = treeModel;
        this.editorProvider = editorProvider;
    }

    public JTree buildFilterTree() {
        var filterTree = new JTree(treeModel);

        filterTree.setRootVisible(false);
        filterTree.setShowsRootHandles(false);
        filterTree.setCellRenderer(nodeRenderer);
        filterTree.setSelectionModel(new DefaultTreeSelectionModel());

        filterTree.setEditable(true);
        filterTree.setCellEditor(editorProvider.get());
        filterTree.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // The filter editing is basically providing an active checkbox to toggle. This checkbox doesn't reflect
                // updates to model made from the outside (e.g. disabling the index window filter by closing its
                // window). To make sure we always show the actual filter state, we actually discard the editing session
                // if the user goes elsewhere.
                // TODO(mlopatkin): The editor keeps a reference to the filter being edited. If the underlying model
                //  changes, then the filter may no longer be in the model. Should we protect ourselves from
                //  accidentally modifying the outdated filter?
                if (filterTree.isEditing()) {
                    filterTree.cancelEditing();
                }
            }
        });

        return filterTree;
    }
}

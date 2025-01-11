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

import name.mlopatkin.andlogview.widgets.UiHelper;
import name.mlopatkin.andlogview.widgets.checktree.CheckFlatTreeUi;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.event.MouseEvent;

import javax.inject.Inject;
import javax.swing.DropMode;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeSelectionModel;

/**
 * Constructs {@link JTree} that shows the available filters.
 */
public class FilterTreeFactory {
    static final ValueRenderer<FilterNodeViewModel> nodeRenderer =
            new ValueRenderer<>(FilterNodeViewModel.class);

    private final FilterNodeRenderer renderer;
    private final TreeModelAdapter treeModel;
    private final TreeActions.Factory treeActionsFactory;

    @Inject
    FilterTreeFactory(
            FilterNodeRenderer renderer,
            TreeModelAdapter treeModel,
            TreeActions.Factory treeActionsFactory) {
        this.renderer = renderer;
        this.treeModel = treeModel;
        this.treeActionsFactory = treeActionsFactory;
    }

    public JTree buildFilterTree(JToolBar filterToolbar) {
        var filterTree = new JTree(treeModel) {
            @Override
            public @Nullable String getToolTipText(MouseEvent event) {
                var hoveredPath = getClosestPathForLocation(event.getX(), event.getY());
                if (hoveredPath == null) {
                    return null;
                }
                var bounds = getPathBounds(hoveredPath);
                if (bounds == null || !bounds.contains(bounds.x, event.getY())) {
                    return null;
                }
                var value = nodeRenderer.toModel(hoveredPath.getLastPathComponent());
                if (value == null) {
                    return null;
                }

                return value.getTooltip();
            }
        };

        ToolTipManager.sharedInstance().registerComponent(filterTree);

        filterTree.setUI(new CheckFlatTreeUi());

        filterTree.setRootVisible(false);
        filterTree.setShowsRootHandles(false);
        filterTree.setCellRenderer(renderer);
        filterTree.setSelectionModel(new DefaultTreeSelectionModel());
        filterTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        filterTree.setDragEnabled(true);
        // TODO(mlopatkin): so far we only support reordering the filters.
        filterTree.setDropMode(DropMode.INSERT);

        filterTree.setTransferHandler(new FilterNodeTransferHandler(treeModel.getModel()));

        var treeActions = treeActionsFactory.create(filterTree);

        configurePopupMenu(filterTree, treeActions);
        configureKeyMap(filterTree, treeActions);
        configureToolBar(filterToolbar, treeActions);

        return filterTree;
    }

    /**
     * Installs the popup menu to the filter tree.
     *
     * @param tree the filter tree to install menu to
     */
    private void configurePopupMenu(JTree tree, TreeActions treeActions) {
        var filterPopupMenu = new JPopupMenu();
        filterPopupMenu.add(treeActions.editSelectedFilter);
        filterPopupMenu.add(treeActions.deleteSelectedFilter);

        var treePopupMenu = new JPopupMenu();
        treePopupMenu.add(treeActions.createFilter).setToolTipText(null);

        UiHelper.PopupMenuDelegate<JTree> menuHandler = (component, x, y) -> {
            assert component == tree;
            var path = tree.getPathForLocation(x, y);
            if (path == null) {
                treePopupMenu.show(tree, x, y);
            } else {
                tree.setSelectionPath(path);
                filterPopupMenu.show(tree, x, y);
            }
        };

        UiHelper.addPopupMenu(tree, menuHandler);
    }

    private void configureKeyMap(JTree tree, TreeActions treeActions) {
        UiHelper.bindKeyFocused(tree, "ENTER", treeActions.editSelectedFilter);
        UiHelper.bindKeyFocused(tree, "SPACE", treeActions.toggleSelectedFilter);
        UiHelper.bindKeyFocused(tree, "DELETE", treeActions.deleteSelectedFilter);
    }

    private void configureToolBar(JToolBar toolBar, TreeActions treeActions) {
        var createFilterBtn = toolBar.add(treeActions.createFilter);
        createFilterBtn.setHideActionText(false);

        createFilterBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        createFilterBtn.setVerticalTextPosition(SwingConstants.CENTER);
    }
}

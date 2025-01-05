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

package name.mlopatkin.andlogview.ui.filtertree;

import name.mlopatkin.andlogview.widgets.UiHelper;

import java.util.Objects;

import javax.inject.Inject;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

/**
 * This class manages the PopupMenu for the FilterTree.
 */
public class PopupMenu {
    private static final ValueRenderer<FilterNodeViewModel> nodeConverter =
            new ValueRenderer<>(FilterNodeViewModel.class);

    private final JPopupMenu noFilterSelected = new JPopupMenu();

    private final FilterTreeModel<FilterNodeViewModel> model;

    @Inject
    public PopupMenu(FilterTreeModel<FilterNodeViewModel> model, FilterCreator filterCreator) {
        this.model = model;

        noFilterSelected.add(UiHelper.makeAction("Create filter", filterCreator::createFilterWithDialog));
    }

    /**
     * Installs the popup menu to the filter tree.
     *
     * @param tree the filter tree to install menu to
     */
    public void configurePopupMenu(JTree tree) {
        var filterPopupMenu = new JPopupMenu();

        filterPopupMenu.add(UiHelper.makeAction("Edit filter", () -> model.editFilter(getSelectedFilter(tree))));

        filterPopupMenu.add(
                UiHelper.makeAction("Delete filter", () -> model.removeFilterForView(getSelectedFilter(tree))));

        UiHelper.PopupMenuDelegate<JTree> menuHandler = (component, x, y) -> {
            assert component == tree;
            var path = tree.getPathForLocation(x, y);
            if (path == null) {
                noFilterSelected.show(tree, x, y);
            } else {
                tree.setSelectionPath(path);
                filterPopupMenu.show(tree, x, y);
            }
        };

        UiHelper.addPopupMenu(tree, menuHandler);
    }

    private static FilterNodeViewModel getSelectedFilter(JTree tree) {
        var path = Objects.requireNonNull(tree.getSelectionPath());
        return Objects.requireNonNull(nodeConverter.toModel(path.getLastPathComponent()));
    }
}

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

import name.mlopatkin.andlogview.ui.Icons;
import name.mlopatkin.andlogview.ui.themes.CurrentTheme;
import name.mlopatkin.andlogview.widgets.UiHelper;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;

import java.awt.event.KeyEvent;
import java.util.function.Consumer;

import javax.swing.Action;
import javax.swing.JTree;

/**
 * A set of Swing {@linkplain Action Actions} that deal with filters in the Tree. Used by the context menu and keyboard
 * shortcuts.
 */
class TreeActions {
    public final Action createFilter;
    public final Action editSelectedFilter;
    public final Action deleteSelectedFilter;
    public final Action toggleSelectedFilter;

    @AssistedInject
    public TreeActions(
            FilterCreator filterCreator,
            FilterTreeModel<FilterNodeViewModel> model,
            CurrentTheme theme,
            @Assisted JTree tree
    ) {
        this.createFilter = UiHelper.makeAction(filterCreator::createFilterWithDialog)
                .name("Create filter…")
                .mnemonic(KeyEvent.VK_C)
                .largeIcon(theme.get().getWidgetFactory().getToolbarIcon(Icons.ADD))
                .shortDescription("Create a new filter with dialog editor")
                .build();
        this.editSelectedFilter =
                UiHelper.makeAction("Edit filter…", () -> withSelectedFilter(tree, model::editFilter));
        this.deleteSelectedFilter =
                UiHelper.makeAction("Delete filter", () -> withSelectedFilter(tree, model::removeFilterForView));
        this.toggleSelectedFilter = UiHelper.makeAction("Toggle filter",
                () -> withSelectedFilter(tree, filter -> model.setFilterEnabled(filter, !filter.isEnabled())));
    }

    private static void withSelectedFilter(JTree tree, Consumer<? super FilterNodeViewModel> filterConsumer) {
        var path = tree.getSelectionPath();
        if (path != null) {
            var filter = FilterTreeFactory.nodeRenderer.toModel(path.getLastPathComponent());
            if (filter != null) {
                filterConsumer.accept(filter);
            }
        }
    }

    @AssistedFactory
    interface Factory {
        TreeActions create(JTree tree);
    }
}

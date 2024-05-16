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

import com.google.common.base.Preconditions;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.inject.Inject;
import javax.swing.AbstractCellEditor;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeCellEditor;

class FilterNodeEditor extends AbstractCellEditor implements TreeCellEditor {
    private final FilterNodeRenderer renderer = new FilterNodeRenderer();
    private final FilterTreeModel<FilterNodeViewModel> treeModel;
    private @Nullable FilterNodeViewModel currentNode;
    private int editingRow = -1;

    @Inject
    public FilterNodeEditor(FilterTreeModel<FilterNodeViewModel> treeModel) {
        this.treeModel = treeModel;
        // Commit checkbox state upon toggling it.
        this.renderer.addCheckboxItemListener(e -> stopCellEditing());
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
            boolean leaf, int row) {
        // TODO(mlopatkin) is it a good approximation of the focus, though?
        var hasFocus = tree.getLeadSelectionRow() == row;
        var component = renderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);

        editingRow = row;
        currentNode = (FilterNodeViewModel) value;

        return component;
    }

    @Override
    public void cancelCellEditing() {
        super.cancelCellEditing();
        editingRow = -1;
        currentNode = null;
    }

    @Override
    public boolean stopCellEditing() {
        if (super.stopCellEditing()) {
            editingRow = -1;
            currentNode = null;
            return true;
        }
        return false;
    }

    @Override
    public EditAction getCellEditorValue() {
        Preconditions.checkState(editingRow != -1 && currentNode != null, "Not in an editing session");
        var enabled = renderer.isSelected();
        var node = currentNode;
        return () -> treeModel.setFilterEnabled(node, enabled);
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        return e == null || (e instanceof MouseEvent mouseEvent && SwingUtilities.isLeftMouseButton(
                mouseEvent));
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return anEvent != null;
    }
}

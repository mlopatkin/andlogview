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

import name.mlopatkin.andlogview.widgets.checktree.Checkable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;

import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

class FilterNodeRenderer implements TreeCellRenderer {
    private final JCheckBox checkBox = new JCheckBox();
    private final JLabel text = new JLabel();
    private final CheckablePanel panel;
    private final TreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

    private final Color selectionForeground;
    private final Color selectionBackground;
    private final Color textForeground;
    private final Color textBackground;
    private final Color selectionInactiveForeground;
    private final Color selectionInactiveBackground;

    @Inject
    public FilterNodeRenderer() {
        panel = new CheckablePanel(checkBox);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(checkBox);
        panel.add(text);

        checkBox.setFocusable(false);
        panel.setFocusable(false);

        var fontValue = UIManager.getFont("Tree.font");
        if (fontValue != null) {
            text.setFont(fontValue);
        }

        selectionInactiveForeground = UIManager.getColor("Tree.selectionInactiveForeground");
        selectionInactiveBackground = UIManager.getColor("Tree.selectionInactiveBackground");
        selectionForeground = UIManager.getColor("Tree.selectionForeground");
        selectionBackground = UIManager.getColor("Tree.selectionBackground");
        textForeground = UIManager.getColor("Tree.textForeground");
        textBackground = UIManager.getColor("Tree.background");
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        var filter = FilterTreeFactory.nodeRenderer.toModel(value);
        if (filter == null) {
            // Sometimes we're asked for the root node even though it is invisible. Fall back to the default renderer in
            // this case.
            return defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }

        var fgColor = getForegroundColor(selected, hasFocus);
        text.setForeground(fgColor);

        var bgColor = getBackgroundColor(selected, hasFocus);
        panel.setBackground(bgColor);

        var dropLocation = tree.getDropLocation();
        if (dropLocation != null && dropLocation.getChildIndex() == -1
                && tree.getRowForPath(dropLocation.getPath()) == row) {
            panel.setBackground(UIManager.getColor("Tree.dropCellBackground"));
            text.setForeground(UIManager.getColor("Tree.dropCellForeground"));
        }

        checkBox.setSelected(filter.isEnabled());
        text.setText(filter.getText());

        if (!checkBox.isValid() || !text.isValid()) {
            // When the panel is detached, it doesn't invalidate itself upon child invalidation. This causes stale
            // measurements to slip when the panel is used as a cell renderer. Manual invalidation forces re-layout.
            // TODO(mlopatkin) Cell renderers disable invalidation even, why do they work properly then?
            panel.invalidate();
        }

        return panel;
    }

    private Color getForegroundColor(boolean selected, boolean hasFocus) {
        if (selected && hasFocus) {
            return selectionForeground;
        } else if (selected) {
            return selectionInactiveForeground;
        }
        return textForeground;
    }

    private Color getBackgroundColor(boolean selected, boolean hasFocus) {
        if (selected && hasFocus) {
            return selectionBackground;
        } else if (selected) {
            return selectionInactiveBackground;
        }
        return textBackground;
    }

    public boolean isSelected() {
        return checkBox.isSelected();
    }

    private static class CheckablePanel extends JPanel implements Checkable {
        private final JComponent checkTarget;

        public CheckablePanel(JComponent checkTarget) {
            this.checkTarget = checkTarget;
        }

        @Override
        public boolean togglesCheckable(Point mouseClickInNode) {
            return checkTarget.getBounds().contains(mouseClickInNode);
        }
    }
}

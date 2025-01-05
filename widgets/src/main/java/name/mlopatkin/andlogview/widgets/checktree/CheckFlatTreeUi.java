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

import name.mlopatkin.andlogview.widgets.FixedTreeUi;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.Objects;

import javax.swing.SwingUtilities;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * A FlatLaF-based Tree UI that can handle checkable nodes.
 */
public class CheckFlatTreeUi extends FixedTreeUi {
    private @Nullable CheckableTreeModel checkableTreeModel;

    @Override
    protected MouseListener createMouseListener() {
        return new CheckTreeMouseListener(super.createMouseListener());
    }

    @Override
    protected void setModel(TreeModel model) {
        if (model instanceof CheckableTreeModel checkable) {
            checkableTreeModel = checkable;
        } else {
            checkableTreeModel = null;
        }
        super.setModel(model);
    }

    private Component createRendererComponent(TreePath path) {
        return currentCellRenderer.getTreeCellRendererComponent(
                tree,
                path.getLastPathComponent(),
                tree.isPathSelected(path),
                tree.isExpanded(path),
                treeModel.isLeaf(path.getLastPathComponent()),
                tree.getRowForPath(path),
                tree.hasFocus());
    }

    private void layoutRenderer(Component renderer, Rectangle bounds) {
        rendererPane.add(renderer);
        renderer.setBounds(bounds);
        renderer.validate();
    }

    private class CheckTreeMouseListener implements MouseListener, MouseMotionListener {
        private final MouseListener parentMouseListener;
        private final MouseMotionListener parentMouseMotionListener;

        private @Nullable TreePath pressedPath;

        public CheckTreeMouseListener(MouseListener parent) {
            this.parentMouseListener = parent;
            this.parentMouseMotionListener = (parent instanceof MouseMotionListener motionListener)
                    ? motionListener
                    : new MouseMotionAdapter() {};
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            parentMouseListener.mouseClicked(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            var checkableModel = checkableTreeModel;
            pressedPath = null;
            if (checkableModel != null && !shouldIgnore(e)) {
                var currentPressedPath = tree.getPathForLocation(e.getX(), e.getY());
                if (currentPressedPath != null) {
                    var node = currentPressedPath.getLastPathComponent();
                    if (checkableModel.isNodeCheckable(node) && isInCheckArea(e, currentPressedPath)) {
                        // Pressed the mouse inside the checkbox area.
                        pressedPath = currentPressedPath;
                    }
                }
            }
            parentMouseListener.mousePressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            var checkableModel = checkableTreeModel;
            var lastPressedPath = pressedPath;
            pressedPath = null;
            if (lastPressedPath != null && checkableModel != null && !shouldIgnore(e)) {
                // Last pressed the mouse inside the checkbox area.
                var releasedPath = tree.getPathForLocation(e.getX(), e.getY());
                if (Objects.equals(lastPressedPath, releasedPath)) {
                    assert releasedPath != null;
                    var node = releasedPath.getLastPathComponent();
                    if (checkableModel.isNodeCheckable(node) && isInCheckArea(e, releasedPath)) {
                        checkableModel.setNodeChecked(node, !checkableModel.isNodeChecked(node));
                    }
                }
            }
            parentMouseListener.mouseReleased(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            parentMouseListener.mouseEntered(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            parentMouseListener.mouseExited(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            parentMouseMotionListener.mouseDragged(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            parentMouseMotionListener.mouseMoved(e);
        }

        private boolean shouldIgnore(MouseEvent e) {
            return !tree.isEnabled() || !SwingUtilities.isLeftMouseButton(e) || e.isConsumed();
        }

        private Point getMouseClickInNode(MouseEvent event, Rectangle nodeBounds) {
            var point = event.getPoint();
            point.translate(-nodeBounds.x, -nodeBounds.y);
            return point;
        }

        private boolean isInCheckArea(MouseEvent event, TreePath path) {
            var rendererComponent = createRendererComponent(path);
            if (rendererComponent instanceof Checkable checkable) {
                var bounds = getPathBounds(tree, path);
                layoutRenderer(rendererComponent, bounds);
                return checkable.togglesCheckable(getMouseClickInNode(event, bounds));
            }
            return false;
        }
    }
}

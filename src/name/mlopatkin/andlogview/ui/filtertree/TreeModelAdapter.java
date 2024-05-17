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

import static name.mlopatkin.andlogview.base.collections.MyArrays.ints;
import static name.mlopatkin.andlogview.base.collections.MyArrays.objects;

import name.mlopatkin.andlogview.widgets.checktree.CheckableTreeModel;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * An adapter for the {@link FilterTreeModel} to use it as a Swing's {@link TreeModel}.
 */
public class TreeModelAdapter implements CheckableTreeModel {
    private final ModelObserverImpl filterModelObserver = new ModelObserverImpl();
    private final FilterTreeModel<FilterNodeViewModel> filterTreeModel;
    private final MutableTreeNode root;

    @SuppressWarnings("unchecked")
    public <V extends FilterNodeViewModel> TreeModelAdapter(FilterTreeModel<V> filterTreeModel) {
        this.filterTreeModel = (FilterTreeModel<FilterNodeViewModel>) filterTreeModel;
        filterTreeModel.asObservable().addObserver(filterModelObserver);

        this.root = new DefaultMutableTreeNode(filterTreeModel);
        filterTreeModel.getFilters().forEach(f -> appendChild(root, createNodeForFilter(f)));
    }

    @Override
    public TreeNode getRoot() {
        return root;
    }

    @Override
    public TreeNode getChild(Object parent, int index) {
        assert parent == getRoot();
        return getRoot().getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent == getRoot()) {
            return getRoot().getChildCount();
        }
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return node != getRoot();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        var action = (EditAction) newValue;
        action.apply();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (parent == getRoot() && child instanceof TreeNode childNode) {
            return getRoot().getIndex(childNode);
        }
        return -1;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        filterModelObserver.addChildListener(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        filterModelObserver.removeChildListener(l);
    }

    @Override
    public boolean isNodeCheckable(Object node) {
        return getRoot() != node;
    }

    @Override
    public boolean isNodeChecked(Object node) {
        return getFilterFromNode(node).isEnabled();
    }

    @Override
    public void setNodeChecked(Object node, boolean checked) {
        var filter = getFilterFromNode(node);
        filterTreeModel.setFilterEnabled(filter, checked);
    }

    private FilterNodeViewModel getFilterFromNode(Object node) {
        if (!(node instanceof DefaultMutableTreeNode treeNode)
                || !(treeNode.getUserObject() instanceof FilterNodeViewModel filter)) {
            throw new IllegalArgumentException("Provided node `" + node + "` is invalid");
        }
        return filter;
    }

    private MutableTreeNode createNodeForFilter(FilterNodeViewModel filter) {
        return new DefaultMutableTreeNode(filter, false);
    }

    private <T extends MutableTreeNode> T appendChild(MutableTreeNode node, T child) {
        node.insert(child, node.getChildCount());
        return child;
    }

    private MutableTreeNode findNodeForFilter(FilterNodeViewModel filter) {
        var enumeration = root.children();
        while (enumeration.hasMoreElements()) {
            var child = enumeration.nextElement();
            if (filter.equals(getFilterFromNode(child))) {
                return (MutableTreeNode) child;
            }
        }
        throw new IllegalArgumentException("Cannot find node for " + filter);
    }

    private void replaceFilter(MutableTreeNode node, FilterNodeViewModel filter) {
        if (!(node instanceof DefaultMutableTreeNode treeNode)
                || !(treeNode.getUserObject() instanceof FilterNodeViewModel)) {
            throw new IllegalArgumentException("Provided node `" + node + "` is invalid");
        }
        treeNode.setUserObject(filter);
    }

    private class ModelObserverImpl
            implements FilterTreeModel.ModelObserver<FilterNodeViewModel> {
        private final Set<TreeModelListener> listeners = new CopyOnWriteArraySet<>();

        public void addChildListener(TreeModelListener listener) {
            listeners.add(listener);
        }

        public void removeChildListener(TreeModelListener listener) {
            listeners.remove(listener);
        }

        @Override
        public void onFilterAdded(FilterNodeViewModel newFilter) {
            var event = addNodeEvent(getRoot(), appendChild(root, createNodeForFilter(newFilter)));

            for (var listener : listeners) {
                listener.treeNodesInserted(event);
            }
        }

        @Override
        public void onFilterRemoved(FilterNodeViewModel filter) {
            var filterNodeToRemove = findNodeForFilter(filter);
            var parent = filterNodeToRemove.getParent();
            var removedIndex = parent.getIndex(filterNodeToRemove);
            filterNodeToRemove.removeFromParent();
            var event = removeNodeEvent(parent, removedIndex, filterNodeToRemove);
            for (var listener : listeners) {
                listener.treeNodesRemoved(event);
            }
        }

        @Override
        public void onFilterReplaced(FilterNodeViewModel oldFilter, FilterNodeViewModel newFilter) {
            var changedChild = findNodeForFilter(oldFilter);
            replaceFilter(changedChild, newFilter);
            var event = changeNodeEvent(getRoot(), changedChild);
            for (var listener : listeners) {
                listener.treeNodesChanged(event);
            }
        }
    }

    private TreeModelEvent addNodeEvent(Object parent, Object newNode) {
        return new TreeModelEvent(this, objects(parent), ints(getIndexOfChild(parent, newNode)), objects(newNode));
    }

    private TreeModelEvent removeNodeEvent(Object parent, int removedIndex, Object removedNode) {
        return new TreeModelEvent(this, objects(parent), ints(removedIndex), objects(removedNode));
    }

    private TreeModelEvent changeNodeEvent(Object parent, Object updatedNode) {
        return new TreeModelEvent(this, objects(parent), ints(getIndexOfChild(parent, updatedNode)),
                objects(updatedNode));
    }
}

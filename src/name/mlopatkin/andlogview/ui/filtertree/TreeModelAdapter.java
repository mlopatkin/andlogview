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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * An adapter for the {@link FilterTreeModel} to use it as a Swing's {@link TreeModel}.
 */
public class TreeModelAdapter implements TreeModel {
    private final FilterTreeModel<?> filterTreeModel;
    private final ModelObserverImpl filterModelObserver;

    public <V extends FilterNodeViewModel> TreeModelAdapter(FilterTreeModel<V> filterTreeModel) {
        this.filterTreeModel = filterTreeModel;
        this.filterModelObserver = new ModelObserverImpl(filterTreeModel.getFilters());
        filterTreeModel.asObservable().addObserver(filterModelObserver);
    }

    @Override
    public FilterTreeModel<?> getRoot() {
        return filterTreeModel;
    }

    @Override
    public FilterNodeViewModel getChild(Object parent, int index) {
        assert parent == getRoot();
        return filterTreeModel.getFilters().get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent == getRoot()) {
            return filterTreeModel.getFilters().size();
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
        if (parent == getRoot()) {
            return filterTreeModel.getFilters().indexOf(child);
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

    private class ModelObserverImpl
            implements FilterTreeModel.ModelObserver<FilterNodeViewModel> {
        private final Set<TreeModelListener> listeners = new CopyOnWriteArraySet<>();
        private final List<FilterNodeViewModel> filters = new ArrayList<>();

        public ModelObserverImpl(Collection<? extends FilterNodeViewModel> filters) {
            this.filters.addAll(filters);
        }

        public void addChildListener(TreeModelListener listener) {
            listeners.add(listener);
        }

        public void removeChildListener(TreeModelListener listener) {
            listeners.remove(listener);
        }

        @Override
        public void onFilterAdded(FilterNodeViewModel newFilter) {
            filters.add(newFilter);

            var event = addNodeEvent(getRoot(), newFilter);
            for (var listener : listeners) {
                listener.treeNodesInserted(event);
            }
        }

        @Override
        public void onFilterRemoved(FilterNodeViewModel filter) {
            var removedIndex = filters.indexOf(filter);
            filters.remove(removedIndex);

            var event = removeNodeEvent(getRoot(), removedIndex, filter);
            for (var listener : listeners) {
                listener.treeNodesRemoved(event);
            }
        }

        @Override
        public void onFilterReplaced(FilterNodeViewModel oldFilter, FilterNodeViewModel newFilter) {
            var changedIndex = filters.indexOf(oldFilter);
            filters.set(changedIndex, newFilter);

            var event = changeNodeEvent(getRoot(), newFilter);
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

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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;

class FilterNodeTransferHandler extends TransferHandler {
    private static final ValueRenderer<FilterNodeViewModel> nodeConverter =
            new ValueRenderer<>(FilterNodeViewModel.class);
    private static final DataFlavor LOCAL_FILTER_FLAVOR;

    static {
        try {
            LOCAL_FILTER_FLAVOR = new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + FilterNodeViewModel.class.getName() + "\"");
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private final FilterTreeModel<FilterNodeViewModel> model;

    public FilterNodeTransferHandler(FilterTreeModel<FilterNodeViewModel> model) {
        this.model = model;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    protected @Nullable Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;
        var selectionPath = tree.getSelectionPath();
        if (selectionPath != null) {
            return new FilterNodeTransferable(
                    Objects.requireNonNull(nodeConverter.toModel(selectionPath.getLastPathComponent())));
        }
        return null;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (!isSupportedTransfer(support)) {
            return false;
        }
        var tree = (JTree) support.getComponent();
        var dropLocation = (JTree.DropLocation) support.getDropLocation();
        var dropPath = dropLocation.getPath();
        // Do not drop nodes onto themselves.
        return dropLocation.getChildIndex() != -1 || !tree.isPathSelected(dropPath);
    }

    private static boolean isSupportedTransfer(TransferSupport support) {
        return (support.isDrop() && support.isDataFlavorSupported(LOCAL_FILTER_FLAVOR));
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        var dropLocation = (JTree.DropLocation) support.getDropLocation();
        var insertBeforePos = dropLocation.getChildIndex();
        try {
            var insertedFilter =
                    (FilterNodeViewModel) support.getTransferable().getTransferData(LOCAL_FILTER_FLAVOR);
            var filters = model.getFilters();
            var insertBefore = insertBeforePos < filters.size() ? filters.get(insertBeforePos) : null;

            // TODO(mlopatkin) actually modify the model.
            return true;
        } catch (UnsupportedFlavorException | IOException e) {
            // This should never happen in practice.
            throw new AssertionError(e);
        }
    }

    private static class FilterNodeTransferable implements Transferable {
        private final FilterNodeViewModel filter;

        public FilterNodeTransferable(FilterNodeViewModel filter) {
            this.filter = filter;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {LOCAL_FILTER_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return FilterNodeViewModel.class.equals(flavor.getRepresentationClass());
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return filter;
        }
    }
}

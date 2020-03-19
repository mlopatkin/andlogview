/*
 * Copyright 2011 Mikhail Lopatkin
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
package org.bitbucket.mlopatkin.android.logviewer;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.ColumnOrder;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.ColumnTogglesModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordClipboardFormatter;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableModel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

public class LogRecordsTransferHandler extends TransferHandler {
    private TransferHandler globalHandler;

    public LogRecordsTransferHandler() {}

    public LogRecordsTransferHandler(TransferHandler globalHandler) {
        this.globalHandler = globalHandler;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (globalHandler != null) {
            return globalHandler.canImport(support);
        } else {
            return super.canImport(support);
        }
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (globalHandler != null) {
            return globalHandler.importData(support);
        } else {
            return super.importData(support);
        }
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        if (!(c instanceof JTable)) {
            return null;
        }
        JTable table = (JTable) c;

        int[] rows = table.getSelectedRows();
        if (rows == null || rows.length == 0) {
            return null;
        }

        LogRecordTableModel model = (LogRecordTableModel) table.getModel();
        ColumnTogglesModel togglesModel = (ColumnTogglesModel) table.getColumnModel();
        LogRecordClipboardFormatter formatter = new LogRecordClipboardFormatter(togglesModel, ColumnOrder.canonical());
        StringBuilder plain = new StringBuilder();
        try {
            for (int row : rows) {
                int modelIndex = table.convertRowIndexToModel(row);
                LogRecord record = model.getRowData(modelIndex);

                formatter.formatLogRecord(modelIndex, record, plain).append('\n');
            }
        } catch (IOException e) {
            throw new AssertionError("StringBuilder throws IOException", e);
        }
        plain.deleteCharAt(plain.length() - 1);
        return new LogRecordTransferable(plain.toString());
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }

    static class LogRecordTransferable implements Transferable {
        private static final DataFlavor[] FLAVORS = {DataFlavor.stringFlavor};
        private String value;

        public LogRecordTransferable(String value) {
            this.value = value;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return value;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return FLAVORS;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for (DataFlavor supportedFlavor : FLAVORS) {
                if (supportedFlavor.equals(flavor)) {
                    return true;
                }
            }
            return false;
        }
    }
}

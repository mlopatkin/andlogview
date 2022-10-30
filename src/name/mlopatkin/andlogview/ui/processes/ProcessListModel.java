/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.processes;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

class ProcessListModel extends AbstractTableModel {
    private static final ImmutableList<Class<?>> COLUMN_CLASSES = ImmutableList.of(Integer.class, String.class);

    static final int COLUMN_PID = 0;
    static final int COLUMN_PROCESS = 1;

    private static final int COLUMNS_COUNT = 2;

    private final List<Map.Entry<Integer, String>> items = new ArrayList<>();
    private final Map<Integer, String> mapper;

    public ProcessListModel(Map<Integer, String> mapper) {
        this.mapper = mapper;
        loadItems();
    }

    private void loadItems() {
        items.clear();
        items.addAll(mapper.entrySet());
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return COLUMN_CLASSES.get(column);
    }

    @Override
    public int getColumnCount() {
        return COLUMNS_COUNT;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        var entry = items.get(rowIndex);
        return switch (columnIndex) {
            case COLUMN_PID -> entry.getKey();
            case COLUMN_PROCESS -> entry.getValue();
            default -> throw new IllegalArgumentException("Invalid column index " + columnIndex);
        };
    }

    public void update() {
        assert SwingUtilities.isEventDispatchThread();
        int oldSize = getRowCount();
        loadItems();
        int newSize = getRowCount();
        int updatedRows = Math.min(oldSize, newSize);
        if (updatedRows > 0) {
            fireTableRowsUpdated(0, updatedRows - 1);
        }
        if (newSize > oldSize) {
            fireTableRowsInserted(oldSize, newSize - 1);
        } else if (newSize < oldSize) {
            fireTableRowsDeleted(newSize, oldSize - 1);
        }
    }
}

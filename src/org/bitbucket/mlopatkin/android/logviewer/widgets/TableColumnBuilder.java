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
package org.bitbucket.mlopatkin.android.logviewer.widgets;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Builder class for {@link TableColumn} to allow chaining.
 */
public class TableColumnBuilder {

    private TableColumn column;

    public TableColumnBuilder(int modelIndex) {
        column = new TableColumn(modelIndex);
    }

    public TableColumnBuilder(int modelIndex, String header) {
        this(modelIndex);
        column.setHeaderValue(header);
    }

    /**
     * @see TableColumn#setPreferredWidth(int)
     */
    public TableColumnBuilder setWidth(int width) {
        column.setPreferredWidth(width);
        return this;
    }

    /**
     * @see TableColumn#setMaxWidth(int)
     */
    public TableColumnBuilder setMaxWidth(int width) {
        column.setMaxWidth(width);
        return this;
    }

    /**
     * @see TableColumn#setCellRenderer(TableCellRenderer)
     */
    public TableColumnBuilder setRenderer(TableCellRenderer renderer) {
        column.setCellRenderer(renderer);
        return this;
    }

    /**
     * @see TableColumn#setCellEditor(TableCellEditor)
     */
    public TableColumnBuilder setEditor(TableCellEditor editor) {
        column.setCellEditor(editor);
        return this;
    }

    /**
     * @see TableColumn#setHeaderValue(Object)
     */
    public TableColumnBuilder setHeader(String header) {
        column.setHeaderValue(header);
        return this;
    }

    public TableColumn build() {
        return column;
    }
}

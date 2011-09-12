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

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

/**
 * Utility methods for common table cell rendering issues: read-only editors,
 * etc.
 */
class TableCellHelper {
    private TableCellHelper() {
    }

    /**
     * Creates read-only {@link JTextField}. Cell editing is toggled with double
     * click.
     * 
     * @return {@link TableCellEditor} over {@link JTextField}
     */
    public static TableCellEditor createReadOnlyCellTextEditor() {
        return new DefaultCellEditor(new JTextField()) {
            public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {
                JTextField tf = (JTextField) super.getTableCellEditorComponent(table, value,
                        isSelected, row, column);
                tf.setEditable(false);
                return tf;
            };

            public boolean isCellEditable(java.util.EventObject anEvent) {
                // only allow double-click to toggle cell editing
                if (anEvent instanceof MouseEvent) {
                    return super.isCellEditable(anEvent);
                } else {
                    return false;
                }
            };
        };
    }
}

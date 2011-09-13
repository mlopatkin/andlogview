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
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.bitbucket.mlopatkin.android.liblogcat.TimeFormatUtils;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;

public class LogRecordTimeCellRenderer extends DefaultTableCellRenderer {
    @Override
    protected void setValue(Object value) {
        if (value == null) {
            super.setValue(null);
            return;
        }
        assert value instanceof Date;
        String strValue = TimeFormatUtils.convertTimeToString((Date) value);
        super.setValue(strValue);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (UiHelper.isTextFit(this, table, row, column, getText())) {
            setToolTipText(null);
        } else {
            setToolTipText(getText());
        }
        return this;
    }
}

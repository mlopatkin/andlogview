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

import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bitbucket.mlopatkin.android.logviewer.Configuration;

public class ToolTippedCellRenderer extends DefaultTableCellRenderer {

    @Override
    protected void setValue(Object value) {
        this.setToolTipText(formatStringToWidth(value.toString()));
        super.setValue(value);
    }

    private static final int MAX_WIDTH = Configuration.ui.tooltipMaxWidth();

    private static String formatStringToWidth(String src) {
        if (src.length() <= MAX_WIDTH) {
            return null;
        }

        StringBuilder result = new StringBuilder("<html>");
        int pos = 0;
        while (pos + MAX_WIDTH < src.length()) {
            String substr = StringEscapeUtils.escapeXml(src.substring(pos, pos + MAX_WIDTH));
            result.append(substr).append("<br>");
            pos += MAX_WIDTH;
        }
        result.append(StringEscapeUtils.escapeXml(src.substring(pos)));
        result.append("</html>");
        return result.toString();
    }
}

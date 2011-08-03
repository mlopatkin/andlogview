package org.bitbucket.mlopatkin.android.logviewer;

import org.apache.commons.lang3.StringEscapeUtils;


public class ToolTippedCellRenderer extends PriorityColoredCellRenderer {

    @Override
    protected void setValue(Object value) {        
        this.setToolTipText(formatStringToWidth(value.toString()));
        super.setValue(value);
    }
    
    private static final int MAX_WIDTH = Configuration.ui.tooltipMaxWidth();

    private static String formatStringToWidth(String src) {
        if (src.length() <= MAX_WIDTH) {
            return src;
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

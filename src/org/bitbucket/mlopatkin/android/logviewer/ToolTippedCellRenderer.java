package org.bitbucket.mlopatkin.android.logviewer;

public class ToolTippedCellRenderer extends PriorityColoredCellRenderer {

    @Override
    protected void setValue(Object value) {
        this.setToolTipText(value.toString());
        super.setValue(value);        
    }
}

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

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.bitbucket.mlopatkin.android.logviewer.widgets.StyledLabel;

public class HighlightCellRenderer extends StyledLabel implements TableCellRenderer,
        TextHighlighter {

    private final static Border NO_BORDER = new EmptyBorder(1, 1, 1, 1);

    private final static Border FOCUSED_BORDER = getFocusedBorder();

    private StyledDocument document = new DefaultStyledDocument();
    private static final Style BASE_STYLE = StyleContext.getDefaultStyleContext().getStyle(
            StyleContext.DEFAULT_STYLE);

    private Style highlighted;

    public HighlightCellRenderer() {
        setBorder(NO_BORDER);
        setDocument(document);

        highlighted = document.addStyle(null, null);
        StyleConstants.setBackground(highlighted, Color.YELLOW);
        StyleConstants.setForeground(highlighted, Color.RED);

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        clearHighlight();
        if (hasFocus) {
            setBorder(FOCUSED_BORDER);
        } else {
            setBorder(NO_BORDER);
        }
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }
        if (value != null) {
            setText(value.toString());
        } else {
            setText(null);
        }
        return this;
    }

    public void invalidate() {
    }

    public void validate() {
    }

    public void revalidate() {
    }

    public void repaint(long tm, int x, int y, int width, int height) {
    }

    public void repaint(Rectangle r) {
    }

    public void repaint() {
    }

    /**
     * Hack to get a consistent focused border from
     * {@link DefaultTableCellRenderer}.
     * 
     * @return the border for focuse cell
     */
    private static Border getFocusedBorder() {
        JTable t = new JTable();
        TableCellRenderer r = t.getDefaultRenderer(Object.class);
        JComponent c = (JComponent) r.getTableCellRendererComponent(t, "value", true, true, 0, 0);
        return c.getBorder();
    }

    @Override
    public void highlightText(int from, int to) {
        document.setCharacterAttributes(from, to - from, highlighted, true);
    }

    @Override
    public void clearHighlight() {
        document.setCharacterAttributes(0, document.getLength(), BASE_STYLE, true);
    }

}

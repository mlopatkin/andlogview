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
package name.mlopatkin.andlogview.ui.logtable;

import name.mlopatkin.andlogview.TooltipGenerator;
import name.mlopatkin.andlogview.search.text.TextHighlighter;
import name.mlopatkin.andlogview.widgets.UiHelper;

import com.formdev.flatlaf.ui.FlatLabelUI;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;

import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class HighlightCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer, TextHighlighter {
    private static final Color HIGHLIGHT_BACKGROUND = Color.YELLOW;
    private static final Color HIGHLIGHT_FOREGROUND = Color.RED;

    private final List<Range<Integer>> highlights = new ArrayList<>();

    @Override
    public void updateUI() {
        super.updateUI();
        setUI(new HighlightUI());
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, @Nullable Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        clearHighlight();
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }
        if (value != null) {
            String text = value.toString();
            if (!UiHelper.isTextFit(this, table, row, column, text)) {
                setToolTipText(new TooltipGenerator(text).getTooltip());
            } else {
                setToolTipText(null);
            }
            setText(text);
        } else {
            setText(null);
            setToolTipText(null);
        }
        return this;
    }

    @Override
    public void highlightText(int from, int to) {
        highlights.add(Range.closedOpen(from, to));
    }

    private void clearHighlight() {
        highlights.clear();
    }

    private static final class HighlightUI extends FlatLabelUI {
        private final HighlightPainter painter = new HighlightPainter() {
            private void paintTextBackground(JLabel l, Graphics g, int textX, int textWidth) {
                g.setColor(HIGHLIGHT_BACKGROUND);
                g.fillRect(textX, 0, textWidth, l.getHeight());
            }

            @Override
            protected void paintHighlightedText(JLabel l, Graphics g, String s, int textX, int textY, int width) {
                paintTextBackground(l, g, textX, width);
                var fg = l.getForeground();
                l.setForeground(HIGHLIGHT_FOREGROUND);
                try {
                    paintText(l, g, s, textX, textY);
                } finally {
                    l.setForeground(fg);
                }
            }

            @Override
            protected void paintText(JLabel l, Graphics g, String s, int textX, int textY) {
                HighlightUI.super.paintEnabledText(l, g, s, textX, textY);
            }
        };

        HighlightUI() {
            super(false);
        }

        @Override
        protected void paintEnabledText(JLabel l, Graphics g, String s, int textX, int textY) {
            if (l instanceof HighlightCellRenderer hcr && !hcr.highlights.isEmpty()) {
                painter.paintText(hcr, g, s, textX, textY, hcr.highlights);
            } else {
                super.paintEnabledText(l, g, s, textX, textY);
            }
        }

        @Override
        protected void paintDisabledText(JLabel l, Graphics g, String s, int textX, int textY) {
            // Disabled text isn't used by this renderer.
            super.paintDisabledText(l, g, s, textX, textY);
        }
    }

    /**
     * This class exists only to test the highlight logic in isolation.
     */
    @VisibleForTesting
    abstract static class HighlightPainter {
        private static final int CLIP_ELLIPSIS_LENGTH = "...".length();

        public void paintText(JLabel l, Graphics g, String s, int textX, int textY,
                Collection<Range<Integer>> highlights) {
            var fm = l.getFontMetrics(l.getFont());
            var isTruncated = !s.equals(l.getText());
            // Text length that doesn't include the truncation mark (...).
            var textLength = isTruncated ? s.length() - CLIP_ELLIPSIS_LENGTH : s.length();

            int lastDrawnPos = 0;
            for (var iter = highlights.iterator(); iter.hasNext() && lastDrawnPos < s.length(); ) {
                var range = iter.next();
                int firstPlain = lastDrawnPos;
                // If the range starts after the truncation, we highlight the mark too.
                int firstHighlighted = Math.min(range.lowerEndpoint(), textLength);

                if (firstPlain < firstHighlighted) {
                    // We don't get there if there are two consecutive highlighted ranges.
                    var plainStr = s.substring(firstPlain, firstHighlighted);
                    paintText(l, g, plainStr, textX, textY);
                    textX += fm.stringWidth(plainStr);
                }

                int highlightedEnd = range.upperEndpoint();
                if (highlightedEnd > textLength) {
                    // If the highlight ends after the truncation point (we check the non-truncated length for that)
                    // then we want to highlight the mark too (so we set the highlight end to include the mark).
                    highlightedEnd = s.length();
                }

                if (firstHighlighted < highlightedEnd) {
                    var highlightedStr = s.substring(firstHighlighted, highlightedEnd);
                    var width = fm.stringWidth(highlightedStr);

                    paintHighlightedText(l, g, highlightedStr, textX, textY, width);

                    textX += width;
                }

                lastDrawnPos = highlightedEnd;
            }

            if (lastDrawnPos < s.length()) {
                paintText(l, g, s.substring(lastDrawnPos), textX, textY);
            }
        }

        protected abstract void paintHighlightedText(JLabel l, Graphics g, String s, int textX, int textY, int width);

        protected abstract void paintText(JLabel l, Graphics g, String s, int textX, int textY);
    }
}

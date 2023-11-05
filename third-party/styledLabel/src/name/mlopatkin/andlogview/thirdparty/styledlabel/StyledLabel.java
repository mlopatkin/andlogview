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

// Original code:
// Copyright © 2003-2012 Stanislav Lapitsky. All Rights Reserved.
// Obtained from http://java-sl.com/wrap.html
// Used under permission (http://java-sl.com/articles.html):
// All the code you can find here or libraries (.jar files) which you can download are free. You can use it or
// extend/modify it as you wish. The only request is to add reference to me in the used code snippets.

package name.mlopatkin.andlogview.thirdparty.styledlabel;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;

import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.FlowView;
import javax.swing.text.FlowView.FlowStrategy;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

// This is an old-style code, I'll fix issues eventually.
@SuppressWarnings("NullAway")
public class StyledLabel extends JTextPane {
    public StyledLabel() {
        setEditorKit(new WrapEditorKit());
    }

    private static class WrapEditorKit extends StyledEditorKit {
        private final ViewFactory defaultFactory = new WrapColumnFactory();

        @Override
        public ViewFactory getViewFactory() {
            return defaultFactory;
        }
    }

    private static class WrapColumnFactory implements ViewFactory {
        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                switch (kind) {
                    case AbstractDocument.ContentElementName:
                        return new CroppingLabelView(elem);
                    case AbstractDocument.ParagraphElementName:
                        return new NoWrapParagraphView(elem);
                    case AbstractDocument.SectionElementName:
                        return new CenteredBoxView(elem, View.Y_AXIS);
                    case StyleConstants.ComponentElementName:
                        return new ComponentView(elem);
                    case StyleConstants.IconElementName:
                        return new IconView(elem);
                }
            }
            return new LabelView(elem);
        }
    }

    private static final FlowStrategy CROPPING_STRATEGY = new FlowStrategy() {
        @Override
        protected int layoutRow(FlowView fv, int rowIndex, int pos) {
            super.layoutRow(fv, rowIndex, pos);
            return fv.getEndOffset();
        }
    };

    private static class NoWrapParagraphView extends ParagraphView {
        public NoWrapParagraphView(Element elem) {
            super(elem);
            strategy = CROPPING_STRATEGY;
        }
    }

    private static class CenteredBoxView extends BoxView {
        public CenteredBoxView(Element elem, int axis) {
            super(elem, axis);
        }

        @Override
        protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
            super.layoutMajorAxis(targetSpan, axis, offsets, spans);
            int textBlockHeight = 0;

            for (int span : spans) {
                textBlockHeight = span;
            }
            int offset = (targetSpan - textBlockHeight) / 2;
            for (int i = 0; i < offsets.length; i++) {
                offsets[i] += offset;
            }
        }
    }

    private static class CroppingLabelView extends LabelView {
        public CroppingLabelView(Element elem) {
            super(elem);
        }

        private FontMetrics fm;
        private static final String ELLIPSIS = "...";

        @SuppressWarnings("deprecation")
        private float getEllipsisWidth() {
            if (fm == null) {
                fm = Toolkit.getDefaultToolkit().getFontMetrics(getFont());
            }
            return fm.stringWidth(ELLIPSIS);
        }

        @Override
        public int getBreakWeight(int axis, float pos, float len) {
            len = len - getEllipsisWidth();
            if (len <= 0) {
                return BadBreakWeight;
            }
            int bw = super.getBreakWeight(axis, pos, len);
            if (bw != BadBreakWeight) {
                return GoodBreakWeight;
            } else {
                return bw;
            }
        }

        private boolean isEllipsisShown;

        @Override
        public View breakView(int axis, int p0, float pos, float len) {
            if (axis == View.X_AXIS) {
                checkPainter();
                float l = len - getEllipsisWidth();
                if (l < 0) {
                    l = 0.0F;
                }
                int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, l);
                // else, no break in the region, return a fragment of the
                // bounded region.
                isEllipsisShown = true;
                if (p0 == getStartOffset() && p1 == getEndOffset()) {
                    return this;
                }

                return createFragment(p0, p1);
            }
            return this;
        }

        @Override
        public float getPreferredSpan(int axis) {
            float span = super.getPreferredSpan(axis);
            if (axis == View.X_AXIS && isEllipsisShown) {
                span += getEllipsisWidth();
            }
            return span;
        }

        @Override
        public void paint(Graphics g, Shape a) {
            super.paint(g, a);
            if (isEllipsisShown) {
                Rectangle alloc = a instanceof Rectangle ? (Rectangle) a : a.getBounds();
                int last = (int) (getPreferredSpan(View.X_AXIS) - getEllipsisWidth());
                Rectangle clip = new Rectangle(alloc.x + last, alloc.y, (int) getEllipsisWidth(), alloc.height);
                Shape oldClip = g.getClip();
                g.setClip(clip);
                g.setFont(getFont());
                if (getContainer() != null) {
                    int charHeight = getContainer().getFontMetrics(getFont()).getHeight();
                    g.drawString(ELLIPSIS, alloc.x + last,
                            alloc.y + fm.getMaxAscent() + (alloc.height - charHeight) / 2);
                } else {
                    g.drawString(ELLIPSIS, alloc.x + last, alloc.y + alloc.height - fm.getMaxDescent());
                }
                g.setClip(oldClip);
            }
        }
    }
}

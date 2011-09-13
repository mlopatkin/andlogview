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

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;

import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class StyledLabel extends JTextPane {

    public StyledLabel() {
        setEditorKit(new WrapEditorKit());
    }

    private static class WrapEditorKit extends StyledEditorKit {
        ViewFactory defaultFactory = new WrapColumnFactory();

        public ViewFactory getViewFactory() {
            return defaultFactory;
        }

    }

    private static class WrapColumnFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new CroppingLabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new NoWrapParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new CenteredBoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }
            return new LabelView(elem);
        }
    }

    private static class NoWrapParagraphView extends ParagraphView {
        public NoWrapParagraphView(Element elem) {
            super(elem);
        }

        public void layout(int width, int height) {
            super.layout(Short.MAX_VALUE, height);
        }

        @Override
        public void paint(Graphics g, Shape a) {
            super.paint(g, a);
        }
    }

    private static class CenteredBoxView extends BoxView {
        public CenteredBoxView(Element elem, int axis) {
            super(elem, axis);
        }

        protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {

            super.layoutMajorAxis(targetSpan, axis, offsets, spans);
            int textBlockHeight = 0;
            int offset = 0;

            for (int i = 0; i < spans.length; i++) {

                textBlockHeight = spans[i];
            }
            offset = (targetSpan - textBlockHeight) / 2;
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
        private static final String ELLIPSIS = "\u2026";

        @SuppressWarnings("deprecation")
        private float getEllipsisWidth() {
            if (fm == null) {
                fm = Toolkit.getDefaultToolkit().getFontMetrics(getFont());
            }
            return fm.stringWidth(ELLIPSIS);
        }

        private boolean isShowEllipsis = false;

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
                if (p0 == getStartOffset() && p1 == getEndOffset()) {
                    return this;
                }
                isShowEllipsis = true;
                return createFragment(p0, p1);
            }
            return this;
        }

        public float getPreferredSpan(int axis) {
            float span = super.getPreferredSpan(axis);
            if (axis == View.X_AXIS && isShowEllipsis) {
                span += getEllipsisWidth();
            }
            return span;
        }

        public void paint(Graphics g, Shape a) {
            super.paint(g, a);
            if (isShowEllipsis) {
                Rectangle alloc = a instanceof Rectangle ? (Rectangle) a : a.getBounds();
                int last = (int) (getPreferredSpan(View.X_AXIS) - getEllipsisWidth());
                Rectangle clip = new Rectangle(alloc.x + last, alloc.y, (int) getEllipsisWidth(),
                        alloc.height);
                Shape oldClip = g.getClip();
                g.setClip(clip);
                g.setFont(getFont());
                int charHeight = 0;
                if (getContainer() != null) {
                    charHeight = getContainer().getFontMetrics(getFont()).getHeight();
                    g.drawString(
                            ELLIPSIS,
                            alloc.x + last,
                            alloc.y + fm.getMaxAscent()
                                    + Math.round((alloc.height - charHeight) / 2));
                } else {
                    g.drawString(ELLIPSIS, alloc.x + last,
                            alloc.y + alloc.height - fm.getMaxDescent());
                }
                g.setClip(oldClip);
            }
        }
    }

    // Debug routines

    private static void dumpView(View v) {
        dumpViews(v);
        System.out.println();
    }

    private static void dumpViews(View v) {
        System.out.println(v + " " + getText(v));
        for (int i = 0; i < v.getViewCount(); ++i) {
            dumpViews(v.getView(i));
        }
    }

    private static String getText(View v) {
        try {
            return v.getDocument().getText(v.getStartOffset(),
                    v.getEndOffset() - v.getStartOffset());
        } catch (BadLocationException e) {
            return "";
        }
    }
}

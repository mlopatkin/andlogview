/*
 * Copyright 2020 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.ui.mainframe.popupmenu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.Icon;

/**
 * The icon that shows a highlight color in the popup menu. A small color-filled square with thin solid border.
 */
public class HighlightIcon implements Icon {
    private static final Color BORDER_COLOR = Color.BLACK;
    private static final int SIZE_PX = 16;
    private static final int BORDER_WIDTH_PX = 1;
    private static final Stroke BORDER_STROKE = new BasicStroke(BORDER_WIDTH_PX);

    private final Color fillColor;


    public HighlightIcon(Color fillColor) {
        this.fillColor = fillColor;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setColor(fillColor);
        // There is a little overdraw, but it is hard to make borders match.
        g2d.fillRect(x, y, SIZE_PX, SIZE_PX);

        g2d.setStroke(BORDER_STROKE);
        g2d.setColor(BORDER_COLOR);
        g2d.drawRect(x, y, SIZE_PX, SIZE_PX);
    }

    @Override
    public int getIconWidth() {
        return SIZE_PX;
    }

    @Override
    public int getIconHeight() {
        return SIZE_PX;
    }
}

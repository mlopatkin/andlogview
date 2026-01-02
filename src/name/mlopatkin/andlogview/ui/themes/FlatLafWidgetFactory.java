/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.themes;

import name.mlopatkin.andlogview.ui.Icons;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.UIScale;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.border.Border;

class FlatLafWidgetFactory implements ThemedWidgetFactory {

    private FlatSVGIcon createIcon(Icons iconId, int iconWidth, int iconHeight) {
        FlatSVGIcon icon = new FlatSVGIcon(iconId.resolveModernPath(), iconWidth, iconHeight);
        FlatSVGIcon.ColorFilter colorFilter = new FlatSVGIcon.ColorFilter();
        colorFilter.add(Color.BLACK, UIManager.getColor("ToggleButton.foreground"));
        icon.setColorFilter(colorFilter);
        return icon;
    }

    @Override
    public ImageIcon getToolbarIcon(Icons iconId) {
        return createIcon(iconId, getToolbarIconSize(), getToolbarIconSize());
    }

    private int getToolbarIconSize() {
        return UIScale.scale(10);
    }

    @Override
    public Border createTopSeparatorBorder() {
        // see https://www.formdev.com/flatlaf/components/separator/
        // I've considered using the full separator drawing routine, with indents and such, but it doesn't appeal
        // visually.
        var foreground = UIManager.getColor("Separator.foreground");
        var stripeThickness = UIManager.getInt("Separator.stripeWidth");  // This is thickness

        return BorderFactory.createMatteBorder(stripeThickness, 0, 0, 0, foreground);
    }
}

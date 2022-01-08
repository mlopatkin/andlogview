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

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

class FlatLafWidgetFactory implements ThemedWidgetFactory {
    @Override
    public ImageIcon getIcon(Icons iconId) {
        FlatSVGIcon icon = new FlatSVGIcon(iconId.resolveModernPath(), getIconWidth(iconId), getIconHeight(iconId));
        FlatSVGIcon.ColorFilter colorFilter = new FlatSVGIcon.ColorFilter();
        colorFilter.add(Color.BLACK, UIManager.getColor("ToggleButton.foreground"));
        icon.setColorFilter(colorFilter);
        return icon;
    }

    @Override
    public void configureFilterPanelButton(AbstractButton button) {
        button.setMargin(new Insets(4, 4, 4, 4));
        button.putClientProperty(FlatClientProperties.BUTTON_TYPE,
                FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
    }

    @Override
    public void configureFilterPanelScrollButton(AbstractButton button) {
        button.setMargin(new Insets(4 , 2, 4, 2));
        button.putClientProperty(FlatClientProperties.BUTTON_TYPE,
                FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
    }

    private int getIconHeight(Icons iconId) {
        return 24;
    }

    private int getIconWidth(Icons iconId) {
        if (iconId == Icons.NEXT || iconId == Icons.PREVIOUS) {
            return 16;
        }
        return 24;
    }
}

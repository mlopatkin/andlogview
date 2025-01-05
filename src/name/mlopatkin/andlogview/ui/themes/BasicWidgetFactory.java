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
import name.mlopatkin.andlogview.widgets.UiHelper;

import java.awt.FlowLayout;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

class BasicWidgetFactory implements ThemedWidgetFactory {
    private static final int SCROLL_BUTTON_WIDTH = 26;

    @Override
    public ImageIcon getIcon(Icons iconId) {
        return new ImageIcon(iconId.getLegacyUrl());
    }

    @Override
    public void configureFilterPanelButton(AbstractButton button) {
    }

    @Override
    public void configureFilterPanelScrollButton(AbstractButton button) {
        UiHelper.setWidths(button, SCROLL_BUTTON_WIDTH);
    }

    @Override
    public void configureFilterPanel(JPanel filterPanel, JPanel filterButtonsPanel) {
        filterButtonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    }

    @Override
    public float scale(float value) {
        return value;
    }

    @Override
    public Border createTopSeparatorBorder() {
        var foreground = UIManager.getColor("Separator.foreground");
        var background = UIManager.getColor("Separator.background");
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, foreground),
                BorderFactory.createMatteBorder(1, 0, 0, 0, background)
        );
    }
}

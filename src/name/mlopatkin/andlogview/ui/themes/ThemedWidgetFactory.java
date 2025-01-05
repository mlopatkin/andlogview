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

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.Border;

public interface ThemedWidgetFactory {
    default ImageIcon getToolbarIcon(Icons iconId) {
        return getIcon(iconId);
    }

    ImageIcon getIcon(Icons iconId);

    void configureFilterPanelButton(AbstractButton button);

    void configureFilterPanelScrollButton(AbstractButton button);

    void configureFilterPanel(JPanel filterPanel, JPanel filterButtonsPanel);

    float scale(float value);

    /**
     * Creates a border that is only drawn on top and can serve as a separator.
     *
     * @return the border
     */
    Border createTopSeparatorBorder();
}

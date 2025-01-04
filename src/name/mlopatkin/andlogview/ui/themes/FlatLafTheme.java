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

import name.mlopatkin.andlogview.widgets.ConfigurableFlatBorder;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.util.ColorFunctions;
import com.google.common.io.Resources;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;

/**
 * FlatLaf L&amp;F with Light Flat IDEA theme.
 */
class FlatLafTheme implements Theme {
    private static final Logger logger = Logger.getLogger(Theme.class);

    @Override
    public String getName() {
        return "flatlaf";
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public boolean install() {
        try {
            IntelliJTheme.setup(Resources.asByteSource(FlatLafThemes.LIGHTFLAT.getUrl()).openStream());
            // This theme shows a thick blue outline when the JTable is focused. This doesn't look good in this app, so
            // we are disabling it by providing a custom border for JScrollPanes wrapping JTables which are displaying
            // the border.
            ConfigurableFlatBorder tableBorder = new ConfigurableFlatBorder();
            tableBorder.focusWidthProp.setDefault(0);
            tableBorder.innerFocusWidthProp.setDefault(0f);
            tableBorder.focusedBorderColorProp.setDefault(UIManager.getColor("Component.borderColor"));
            UIManager.put("Table.scrollPaneBorder", tableBorder);

            var selectionBackground = UIManager.getColor("Tree.selectionBackground");
            var dropBackground = ColorFunctions.lighten(selectionBackground, 0.1f);
            UIManager.put("Tree.dropCellBackground", dropBackground);
            UIManager.put("Tree.dropLineColor", dropBackground);

        } catch (IOException e) {
            logger.error(
                    String.format("Failed to load %s theme", FlatLafThemes.LIGHTFLAT.name().toLowerCase(Locale.ROOT)),
                    e);
            return false;
        }
        return true;
    }

    @Override
    public ThemedWidgetFactory getWidgetFactory() {
        return new FlatLafWidgetFactory();
    }

    @Override
    public boolean supportsFilterTreeView() {
        return true;
    }
}

/*
 * Copyright 2026 the Andlogview authors
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

import name.mlopatkin.andlogview.widgets.BorderPanel;
import name.mlopatkin.andlogview.widgets.ClientProperty;

import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;

/**
 * Client properties that are theme-dependent. These are updated automatically when you change the theme.
 */
public final class ThemeProperties {
    private ThemeProperties() {}

    /**
     * Top-only border for a panel. The border serves as a separator between the panel and anything on top of it.
     *
     * @see BorderPanel
     */
    public static final ClientProperty<Border> panelWithTopSeparatorBorder = ClientProperty.create(Border.class);

    static void updateDefaults() {
        panelWithTopSeparatorBorder.setDefault(createTopSeparatorBorder());
    }

    private static Border createTopSeparatorBorder() {
        // see https://www.formdev.com/flatlaf/components/separator/
        // I've considered using the full separator drawing routine, with indents and such, but it doesn't appeal
        // visually.
        var foreground = UIManager.getColor("Separator.foreground");
        var stripeThickness = UIManager.getInt("Separator.stripeWidth");  // This is thickness despite the name

        return new BorderUIResource.MatteBorderUIResource(stripeThickness, 0, 0, 0, foreground);
    }
}

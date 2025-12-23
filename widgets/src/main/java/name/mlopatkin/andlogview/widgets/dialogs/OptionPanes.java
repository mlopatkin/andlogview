/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview.widgets.dialogs;

import javax.swing.JOptionPane;

public final class OptionPanes {
    private OptionPanes() {}

    /**
     * Creates a JOptionPane with the default error icon.
     *
     * @param title the dialog title
     * @return a builder to further configure
     */
    public static ErrorOptionPaneBuilder error(String title) {
        return new OptionPaneBuilderImpl(title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Creates a JOptionPane with the default warning icon.
     *
     * @param title the dialog title
     * @return a builder to further configure
     */
    public static OptionPaneBuilder warning(String title) {
        return new OptionPaneBuilderImpl(title, JOptionPane.WARNING_MESSAGE);
    }
}

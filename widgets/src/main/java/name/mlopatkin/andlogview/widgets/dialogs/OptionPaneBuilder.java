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

import org.jspecify.annotations.Nullable;

import java.awt.Component;

import javax.swing.JComponent;

public interface OptionPaneBuilder {
    /**
     * Sets the string as the message. You can use either this or {@link #messageContent(JComponent)}.
     *
     * @param message the primary message
     * @return this
     */
    OptionPaneBuilder message(String message);

    /**
     * Sets the provided component as the message. You can use either this or {@link #message(String)}.
     *
     * @param messageContent the primary message
     * @return this
     */
    OptionPaneBuilder messageContent(JComponent messageContent);

    /**
     * Adds extra content line. The message can be a string or a {@link JComponent}.
     *
     * @param message the extra content
     * @return this
     */
    OptionPaneBuilder extraMessage(Object message);

    /**
     * Adds initial option. This option will be focused when the dialog shows. At most one option can be initial.
     * Use {@link #addCancelOptionAsInitial(String, Runnable)} if the initial option also cancels the dialog.
     *
     * @param title the option title, e.g. "OK"
     * @param action the action to run if the option is selected by the user
     * @return this
     */
    OptionPaneBuilder addInitialOption(String title, Runnable action);

    /**
     * Adds an option.
     *
     * @param title the option title
     * @param action the action to run if the option is selected by the user
     * @return this
     */
    OptionPaneBuilder addOption(String title, Runnable action);

    /**
     * Adds cancel option. The action will also run if the dialog is closed without selecting an option.
     * There can be at most one cancel option.
     *
     * @param title the option title, e.g. "Cancel"
     * @param action the action to run if the option is selected by the user or if the dialog is closed
     * @return this
     */
    OptionPaneBuilder addCancelOption(String title, Runnable action);

    /**
     * Adds cancel option and makes it initial.
     * The action will also run if the dialog is closed without selecting an option.
     * No other cancel or initial options can be provided.
     *
     * @param title the option title, e.g. "Cancel"
     * @param action the action to run if the option is selected by the user or if the dialog is closed
     * @return this
     */
    OptionPaneBuilder addCancelOptionAsInitial(String title, Runnable action);

    /**
     * Shows the dialog in the context of the parent frame. Blocks until the dialog is dismissed. This method will
     * run
     * the selected action.
     *
     * @param parent the optional parent frame
     */
    void show(@Nullable Component parent);
}

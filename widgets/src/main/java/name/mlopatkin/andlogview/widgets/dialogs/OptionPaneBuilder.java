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

import com.google.common.base.Preconditions;

import org.jspecify.annotations.Nullable;

import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * Builder class to set up complex {@link JOptionPane} with multiple custom actions.
 */
public abstract class OptionPaneBuilder<T extends OptionPaneBuilder<T>> {
    private final String title;
    private final int messageType;

    private final Map<String, Runnable> actions = new LinkedHashMap<>();
    private final List<Object> messageElements = new ArrayList<>();

    private @Nullable String initialOption;
    private @Nullable Runnable cancelAction;

    protected OptionPaneBuilder(String title, int messageType) {
        this.title = title;
        this.messageType = messageType;
    }

    protected abstract T self();

    private static class Impl extends OptionPaneBuilder<Impl> {
        private Impl(String title, int messageType) {
            super(title, messageType);
        }

        @Override
        protected Impl self() {
            return this;
        }
    }

    /**
     * Creates a JOptionPane with the default error icon.
     *
     * @param title the dialog title
     * @return a builder to further configure
     */
    public static OptionPaneBuilder<?> error(String title) {
        return new Impl(title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Creates a JOptionPane with the default warning icon.
     *
     * @param title the dialog title
     * @return a builder to further configure
     */
    public static OptionPaneBuilder<?> warning(String title) {
        return new Impl(title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Sets the string as the message. You can use either this or {@link #messageContent(JComponent)}.
     *
     * @param message the primary message
     * @return this
     */
    public T message(String message) {
        Preconditions.checkState(messageElements.isEmpty(), "Single string message element can only be the first");
        messageElements.add(message);
        return self();
    }

    /**
     * Sets the provided component as the message. You can use either this or {@link #message(String)}.
     *
     * @param messageContent the primary message
     * @return this
     */
    public T messageContent(JComponent messageContent) {
        Preconditions.checkState(messageElements.isEmpty(), "Single message content element can only be the first");
        messageElements.add(messageContent);
        return self();
    }

    /**
     * Adds extra content line. The message can be a string or a {@link JComponent}.
     *
     * @param message the extra content
     * @return this
     */
    public T extraMessage(Object message) {
        Preconditions.checkState(!messageElements.isEmpty(), "Extra message element cannot be added before main");
        messageElements.add(message);
        return self();
    }

    /**
     * Adds initial option. This option will be focused when the dialog shows. At most one option can be initial.
     * Use {@link #addCancelOptionAsInitial(String, Runnable)} if the initial option also cancels the dialog.
     *
     * @param title the option title, e.g. "OK"
     * @param action the action to run if the option is selected by the user
     * @return this
     */
    public T addInitialOption(String title, Runnable action) {
        Preconditions.checkState(initialOption == null, "Can't add another initial option");
        actions.put(title, action);
        initialOption = title;
        return self();
    }

    /**
     * Adds an option.
     *
     * @param title the option title
     * @param action the action to run if the option is selected by the user
     * @return this
     */
    public T addOption(String title, Runnable action) {
        actions.put(title, action);
        return self();
    }

    /**
     * Adds cancel option. The action will also run if the dialog is closed without selecting an option.
     * There can be at most one cancel option.
     *
     * @param title the option title, e.g. "Cancel"
     * @param action the action to run if the option is selected by the user or if the dialog is closed
     * @return this
     */
    public T addCancelOption(String title, Runnable action) {
        Preconditions.checkState(cancelAction == null, "Can't add another cancel action");
        actions.put(title, action);
        cancelAction = action;
        return self();
    }
    /**
     * Adds cancel option and makes it initial.
     * The action will also run if the dialog is closed without selecting an option.
     * No other cancel or initial options can be provided.
     *
     * @param title the option title, e.g. "Cancel"
     * @param action the action to run if the option is selected by the user or if the dialog is closed
     * @return this
     */
    public T addCancelOptionAsInitial(String title, Runnable action) {
        Preconditions.checkState(initialOption == null, "Can't add another initial option");
        initialOption = title;
        return addCancelOption(title, action);
    }

    private Object getContents() {
        Preconditions.checkState(!messageElements.isEmpty(), "No contents provided");
        if (messageElements.size() == 1) {
            return messageElements.get(0);
        }

        return messageElements.toArray(new Object[0]);
    }

    private Object @Nullable [] getOptions() {
        if (actions.isEmpty()) {
            return null;
        }
        return actions.keySet().toArray(new String[0]);
    }

    protected void prepareDialog(JDialog dialog) {}

    /**
     * Shows the dialog in the context of the parent frame. Blocks until the dialog is dismissed. This method will run
     * the selected action.
     *
     * @param parent the optional parent frame
     */
    public void show(@Nullable Component parent) {
        @SuppressWarnings("MagicConstant")
        var optionPane = new JOptionPane(
                getContents(),
                messageType,
                JOptionPane.DEFAULT_OPTION, // Don't use custom options
                null, // Use default icon
                getOptions(),
                initialOption
        );

        var dialog = optionPane.createDialog(parent, title);
        prepareDialog(dialog);
        dialog.setVisible(true);

        var result = optionPane.getValue();
        if (result == null) {
            if (cancelAction != null) {
                cancelAction.run();
            }
            return;
        }

        @SuppressWarnings("SuspiciousMethodCalls")
        var actionToRun = actions.get(result);
        if (actionToRun != null) {
            actionToRun.run();
        }
    }
}

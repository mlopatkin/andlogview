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

import static name.mlopatkin.andlogview.widgets.MigConstraints.CC;
import static name.mlopatkin.andlogview.widgets.MigConstraints.LC;

import com.google.common.base.Preconditions;

import net.miginfocom.swing.MigLayout;

import org.jspecify.annotations.Nullable;

import java.awt.Component;
import java.awt.Font;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * A universal implementation of the {@link OptionPaneBuilder} and {@link ErrorOptionPaneBuilder}.
 */
class OptionPaneBuilderImpl implements ErrorOptionPaneBuilder {
    private final String title;
    private final int messageType;

    private final Map<String, Runnable> actions = new LinkedHashMap<>();
    private final List<Object> messageElements = new ArrayList<>();

    private @Nullable DetailsPanel detailsPanel;
    private @Nullable String initialOption;
    private @Nullable Runnable cancelAction;

    OptionPaneBuilderImpl(String title, int messageType) {
        this.title = title;
        this.messageType = messageType;
    }

    @Override
    public ErrorOptionPaneBuilder message(String message) {
        Preconditions.checkState(messageElements.isEmpty(), "Single string message element can only be the first");
        messageElements.add(message);
        return this;
    }

    @Override
    public ErrorOptionPaneBuilder messageContent(JComponent messageContent) {
        Preconditions.checkState(messageElements.isEmpty(), "Single message content element can only be the first");
        messageElements.add(messageContent);
        return this;
    }

    @Override
    public ErrorOptionPaneBuilder extraMessage(Object message) {
        Preconditions.checkState(!messageElements.isEmpty(), "Extra message element cannot be added before main");
        messageElements.add(message);
        return this;
    }

    @Override
    public ErrorOptionPaneBuilder addInitialOption(String title, Runnable action) {
        Preconditions.checkState(initialOption == null, "Can't add another initial option");
        actions.put(title, action);
        initialOption = title;
        return this;
    }

    @Override
    public ErrorOptionPaneBuilder addOption(String title, Runnable action) {
        actions.put(title, action);
        return this;
    }

    @Override
    public ErrorOptionPaneBuilder addCancelOption(String title, Runnable action) {
        Preconditions.checkState(cancelAction == null, "Can't add another cancel action");
        actions.put(title, action);
        cancelAction = action;
        return this;
    }

    @Override
    public ErrorOptionPaneBuilder addCancelOptionAsInitial(String title, Runnable action) {
        Preconditions.checkState(initialOption == null, "Can't add another initial option");
        initialOption = title;
        return addCancelOption(title, action);
    }

    @Override
    public ErrorOptionPaneBuilder details(Throwable exception) {
        Preconditions.checkState(detailsPanel == null, "Details is already set");
        detailsPanel = new DetailsPanel(exception);
        return extraMessage(detailsPanel.createContentPanel());
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

    @Override
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
        if (detailsPanel != null) {
            detailsPanel.dialog = dialog;
        }
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

    private static class DetailsPanel {
        private final JScrollPane stackTraceScrollPane;
        private final JButton detailsButton;

        @Nullable JDialog dialog;

        public DetailsPanel(Throwable exception) {
            this.detailsButton = createDetailsToggleButton();
            this.stackTraceScrollPane = createStackTracePanel(exception);
        }

        private JButton createDetailsToggleButton() {
            var button = new JButton("Details >>");
            button.addActionListener(e -> toggleDetails());
            return button;
        }

        private JScrollPane createStackTracePanel(Throwable exception) {
            var stackTraceArea = new JTextArea(formatStackTrace(exception));
            stackTraceArea.setEditable(false);
            stackTraceArea.setLineWrap(false);        // No wrapping for horizontal scroll
            stackTraceArea.setWrapStyleWord(false);
            stackTraceArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

            var scrollPane = new JScrollPane(stackTraceArea);
            scrollPane.setVisible(false);   // Initially collapsed

            return scrollPane;
        }

        private void toggleDetails() {
            boolean isCurrentlyVisible = stackTraceScrollPane.isVisible();
            boolean willBeVisible = !isCurrentlyVisible;

            stackTraceScrollPane.setVisible(willBeVisible);
            detailsButton.setText(willBeVisible ? "Details <<" : "Details >>");

            Objects.requireNonNull(dialog).pack();
        }

        JPanel createContentPanel() {
            var contentPanel = new JPanel();
            contentPanel.setLayout(new MigLayout(
                    LC().insets("panel").wrapAfter(1).fillX().width("600lp")
            ));

            contentPanel.add(detailsButton, CC().alignX("left").wrap("related"));

            contentPanel.add(stackTraceScrollPane,
                    CC().growX().growY().minHeight("0").maxHeight("600lp").hideMode(3).wrap()
            );

            return contentPanel;
        }

        private static String formatStackTrace(Throwable exception) {
            var sw = new StringWriter();
            var pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            return sw.toString();
        }
    }
}

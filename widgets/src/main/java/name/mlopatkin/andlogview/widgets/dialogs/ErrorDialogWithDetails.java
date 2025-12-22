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

import net.miginfocom.swing.MigLayout;

import org.jspecify.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Shows an error dialog with an expandable stacktrace for exception.
 */
public class ErrorDialogWithDetails {
    private final JDialog dialog;
    private final JScrollPane stackTraceScrollPane;
    private final JButton detailsButton;

    public ErrorDialogWithDetails(@Nullable Component owner, String message, Throwable exception) {
        this(owner, "Error", message, exception);
    }

    public ErrorDialogWithDetails(@Nullable Component owner, String title, String message, Throwable exception) {
        // Create components first
        var messagePanel = createMessagePanel(message);
        this.detailsButton = createDetailsToggleButton();
        this.stackTraceScrollPane = createStackTracePanel(exception);

        // Create content panel with all components
        var contentPanel = createContentPanel(messagePanel, detailsButton, stackTraceScrollPane);

        var optionPane = new JOptionPane(
                contentPanel,
                JOptionPane.ERROR_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,  // Use default error icon from L&F
                new String[] {"OK"}
        );

        dialog = optionPane.createDialog(owner, title);
        dialog.setResizable(true);
    }

    public void show() {
        dialog.setVisible(true);
    }

    private JPanel createContentPanel(
            JPanel messagePanel,
            JButton detailsButton,
            JScrollPane stackTraceScrollPane
    ) {
        var contentPanel = new JPanel();
        contentPanel.setLayout(new MigLayout(
                LC().insets("0").wrapAfter(1).fillX().width("600lp")
        ));

        contentPanel.add(messagePanel, CC().growX().wrap());
        contentPanel.add(detailsButton, CC().alignX("left").wrap());

        contentPanel.add(stackTraceScrollPane,
                CC().growX().growY().minHeight("0").maxHeight("300lp").hideMode(3).wrap()
        );

        return contentPanel;
    }

    private JPanel createMessagePanel(String message) {
        var messagePanel = new JPanel(new BorderLayout());

        var messageLabel = new JLabel(message);
        messagePanel.add(messageLabel, BorderLayout.CENTER);

        return messagePanel;
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

        dialog.pack();
    }

    private static String formatStackTrace(Throwable exception) {
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }

    public static void show(@Nullable Component owner, String message, Throwable exception) {
        var dialog = new ErrorDialogWithDetails(owner, message, exception);
        dialog.show();
    }
}

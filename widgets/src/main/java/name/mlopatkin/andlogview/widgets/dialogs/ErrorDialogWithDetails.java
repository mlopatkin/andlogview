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
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Shows an error dialog with an expandable stacktrace for exception.
 */
public class ErrorDialogWithDetails extends OptionPanes.OptionPaneBuilder<ErrorDialogWithDetails> {
    private @Nullable DetailsPanel detailsPanel;

    private ErrorDialogWithDetails(String title) {
        super(title, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    protected ErrorDialogWithDetails self() {
        return this;
    }

    @Override
    protected void prepareDialog(JDialog dialog) {
        super.prepareDialog(dialog);

        if (detailsPanel != null) {
            detailsPanel.dialog = dialog;
        }
    }

    public ErrorDialogWithDetails details(Throwable exception) {
        Preconditions.checkState(detailsPanel == null, "Details is already set");
        detailsPanel = new DetailsPanel(exception);
        return extraMessage(detailsPanel.createContentPanel());
    }

    public static ErrorDialogWithDetails error(String title) {
        return new ErrorDialogWithDetails(title);
    }

    public static void show(@Nullable Component owner, String message, Throwable exception) {
        error("Error")
                .message(message)
                .details(exception)
                .show(owner);
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

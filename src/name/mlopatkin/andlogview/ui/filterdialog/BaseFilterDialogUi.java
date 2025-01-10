/*
 * Copyright 2014 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.ui.filterdialog;

import static name.mlopatkin.andlogview.widgets.MigConstraints.CC;
import static name.mlopatkin.andlogview.widgets.MigConstraints.LC;

import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.logmodel.LogRecord;

import net.miginfocom.swing.MigLayout;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Base Filter Dialog UI class.
 */
class BaseFilterDialogUi extends JDialog {
    protected final JTextField nameTextField;
    protected final JTextField tagTextField;
    protected final JTextField messageTextField;
    protected final JTextField pidTextField;

    protected final JComboBox<LogRecord.Priority> logLevelList;

    protected final FilteringModesPanel modesPanel;
    protected final JComboBox<ColorsComboBoxModel.Item> colorsList;

    protected final JButton okButton;
    protected final JButton cancelButton;

    public BaseFilterDialogUi(Frame owner) {
        super(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        getContentPane().setLayout(new BorderLayout());
        var content = new JPanel(new MigLayout(
                LC().wrapAfter(1).fillX().insets("8lp").width("400lp"))
        );
        getContentPane().add(content, BorderLayout.CENTER);

        nameTextField = addEntry(content, "Name", new JTextField());
        tagTextField = addEntry(content, "Tags to filter", new JTextField());
        messageTextField = addEntry(content, "Message text to filter", new JTextField());
        pidTextField = addEntry(content, "PIDs or app names to filter", new JTextField());
        logLevelList = addEntry(content, "Log level", new JComboBox<>(new PriorityComboBoxModel()));

        modesPanel = new FilteringModesPanel();
        colorsList = new JComboBox<>(new ColorsComboBoxModel());
        colorsList.setSelectedIndex(0);

        modesPanel.getButtons().forEach((mode, button) -> {
            var isHighlight = FilteringMode.HIGHLIGHT.equals(mode);
            if (isHighlight) {
                content.add(button, CC().split());
                content.add(colorsList, CC().alignX("left").alignY("baseline").wrap("0"));
            } else {
                content.add(button, CC().wrap("0"));
            }
        });

        var buttonPanel = new JPanel(new MigLayout(LC().alignX("right").insets("8lp")));
        okButton = new JButton("OK");
        buttonPanel.add(okButton, CC().tag("ok").cell(0, 0));
        cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton, CC().tag("cancel").cell(0, 0));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(getParent());
    }

    private <T extends JComponent> T addEntry(Container container, String label, T entry) {
        container.add(new JLabel(label));
        container.add(entry, CC().growX());
        return entry;
    }
}

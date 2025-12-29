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

import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;

import javax.inject.Provider;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
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

    public BaseFilterDialogUi(Frame owner, Provider<ColorsComboBoxModel> colorsModel) {
        super(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        getContentPane().setLayout(new BorderLayout());
        var content = getContentPane();
        content.setLayout(new MigLayout(
                LC().insets("dialog").wrapAfter(1).fillX().width("400lp"))
        );

        nameTextField = addEntry(content, "Name", new JTextField(), "optional, e.g. MyFilter");
        tagTextField = addEntry(content, "Tags to filter", new JTextField(), "optional, e.g. ActivityManager, /^cr_/");
        messageTextField = addEntry(content, "Message text to filter", new JTextField(),
                "optional, e.g. foo or /www\\.\\w+\\.com/");
        pidTextField = addEntry(content, "PIDs or app names to filter", new JTextField(),
                "optional, e.g. 1337, com.example.app, /:sandboxed_process\\d+/");
        logLevelList = addEntry(content, "Log level", new JComboBox<>(new PriorityComboBoxModel()), "optional");

        modesPanel = new FilteringModesPanel();
        colorsList = new JComboBox<>(colorsModel.get());
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

        content.add(Box.createVerticalGlue(), CC().wrap("0 push"));

        okButton = new JButton("OK");
        content.add(okButton, CC().split().alignX("right"));
        getRootPane().setDefaultButton(okButton);

        cancelButton = new JButton("Cancel");
        content.add(cancelButton);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(getParent());
    }

    private <T extends JComponent> T addEntry(Container container, String label, T entry, String placeholder) {
        container.add(new JLabel(label));
        container.add(entry, CC().growX());
        entry.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        return entry;
    }
}

/*
 * Copyright 2021 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.ui.preferences;

import static name.mlopatkin.andlogview.widgets.MigConstraints.CC;
import static name.mlopatkin.andlogview.widgets.MigConstraints.LC;

import name.mlopatkin.andlogview.utils.CommonChars;
import name.mlopatkin.andlogview.widgets.UiHelper;

import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;

import java.awt.Container;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

abstract class ConfigurationDialogUi extends JDialog {
    protected final ButtonGroup themeSelector = new ButtonGroup();
    protected final JTextField adbExecutableText = new JTextField(25);
    protected final JButton browseAdbBtn = new JButton(String.valueOf(CommonChars.ELLIPSIS));
    protected final JCheckBox autoReconnectCheckbox = new JCheckBox("Reconnect to device automatically");
    protected final JButton installAdbBtn = new JButton(UiHelper.makeAction("Install ADB…", this::onInstallAdb));
    protected final Action okAction = UiHelper.makeAction("OK", this::onPositiveResult);
    protected final Action cancelAction = UiHelper.makeAction("Cancel", this::onNegativeResult);

    public ConfigurationDialogUi(Frame owner, String selectedTheme, List<String> themes, boolean darkModeEnabled) {
        super(owner, true);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("Configuration");

        var content = getContentPane();
        content.setLayout(new MigLayout(
                LC().insets("dialog").fillX().wrapAfter(2)
        ));

        if (darkModeEnabled) {
            setUpAppearanceSection(content, selectedTheme, themes);
        }

        setUpAdbSection(content);
        setUpDialogButtonsRow(content);

        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(getSize());

        UiHelper.bindKeyGlobal(this, KeyEvent.VK_ESCAPE, "close", cancelAction);
    }

    private void setUpAppearanceSection(Container content, String selectedTheme, List<String> themes) {
        beginSection(content, "Appearance");
        List<JRadioButton> themeRadioButtons = themes.stream().map(this::newThemeButton).toList();

        themeRadioButtons.forEach(themeSelector::add);

        var selectedThemeButton = themeRadioButtons.get(themes.indexOf(selectedTheme));
        themeSelector.setSelected(selectedThemeButton.getModel(), true);

        var label = new JLabel("Theme:");
        var themeCount = themeRadioButtons.size();
        content.add(label, indent().span(2).split(themeCount + 1));

        for (int i = 0; i < themeCount - 1; i++) {
            content.add(themeRadioButtons.get(i));
        }
        content.add(themeRadioButtons.get(themeCount - 1), CC().wrap("unrelated"));
    }

    private JRadioButton newThemeButton(String theme) {
        var button = new JRadioButton(theme);
        button.setActionCommand(theme);
        return button;
    }

    private void setUpAdbSection(Container content) {
        beginSection(content, "ADB");
        JLabel adbExecutableTextLabel = new JLabel("ADB executable location");
        adbExecutableTextLabel.setLabelFor(adbExecutableText);

        content.add(adbExecutableTextLabel, indent().alignX("label"));
        content.add(adbExecutableText, CC().split().growX().pushX());
        content.add(browseAdbBtn, CC().wrap());

        content.add(installAdbBtn, indent().hideMode(3).wrap());

        content.add(autoReconnectCheckbox, indent().spanX(2).wrap("0 push"));
    }

    private void setUpDialogButtonsRow(Container content) {
        JButton okButton = new JButton(okAction);
        content.add(okButton, CC().spanX(2).split().alignX("right"));
        getRootPane().setDefaultButton(okButton);

        content.add(new JButton(cancelAction));
    }

    private void beginSection(Container content, String panelTitle) {
        JLabel adbLabel = new JLabel(panelTitle);
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        // For some reason, the label may appear clipped. Extra space helps with that.
        content.add(adbLabel, CC().span(2).split().minWidth("pref + 2lp"));
        content.add(sep, CC().growX().wrap("related"));
    }

    private CC indent() {
        return CC().gapBefore("20lp");
    }

    protected abstract void onPositiveResult();

    protected abstract void onNegativeResult();

    protected abstract void onInstallAdb();
}

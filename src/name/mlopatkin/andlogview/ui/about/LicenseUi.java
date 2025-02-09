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

package name.mlopatkin.andlogview.ui.about;

import static name.mlopatkin.andlogview.widgets.MigConstraints.CC;
import static name.mlopatkin.andlogview.widgets.MigConstraints.LC;

import net.miginfocom.swing.MigLayout;

import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class LicenseUi extends JDialog {
    public LicenseUi(Window owner, OssComponent ossComponent) {
        super(owner, "License for " + ossComponent.getName(), ModalityType.APPLICATION_MODAL);

        var content = getContentPane();
        // Max height is to prevent the dialog from growing too tall.
        content.setLayout(new MigLayout(
                LC().insets("dialog").wrapAfter(1).fillX().maxHeight("600lp"))
        );

        var scope = ossComponent.getScope();
        if (scope.contains("\n")) {
            scope = "\n" + scope;
        }
        var text = new JTextArea(
                "License for " + scope + "\n\n" + ossComponent.getLicenseText()
        );
        text.setEditable(false);

        var scrollPane = new JScrollPane(text);
        // Lame trick to always reserve some space for the scroll bar, so it doesn't cause content to wrap when it
        // appears.
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        content.add(scrollPane, CC().grow().wrap("related push"));

        var okButton = new JButton("OK");
        okButton.addActionListener(e -> dispose());
        content.add(okButton, CC().alignX("right"));
        getRootPane().setDefaultButton(okButton);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(getParent());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }
}

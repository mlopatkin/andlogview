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

import name.mlopatkin.andlogview.BuildInfo;
import name.mlopatkin.andlogview.Main;

import net.miginfocom.swing.MigLayout;

import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;

public class LicensesUi extends JDialog {
    public LicensesUi(Window owner) {
        super(owner, "List of third-party libraries used in " + Main.APP_NAME + " " + BuildInfo.VERSION,
                ModalityType.APPLICATION_MODAL);

        var content = getContentPane();
        content.setLayout(new MigLayout(
                LC().insets("dialog").wrapAfter(1).fillX().width("400lp"))
        );

        var text = new JEditorPane("text/html", """
                <html>
                <table>
                <tr>
                <th>Component</th>
                <th>License</th>
                </tr>
                <tr>
                <td><a href="https://adoptium.net/">Eclipse Temurin Java Runtime</a></td>
                <td>GPL v2</td>
                </tr>
                </table>
                </html>
                """);
        text.setEditable(false);

        content.add(text, CC().grow().wrap("push"));

        var okButton = new JButton("OK");
        okButton.addActionListener(e -> dispose());
        content.add(okButton, CC().alignX("right"));
        getRootPane().setDefaultButton(okButton);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(getParent());
    }
}

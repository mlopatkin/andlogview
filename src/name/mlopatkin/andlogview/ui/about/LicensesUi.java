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
import name.mlopatkin.andlogview.utils.MyFutures;
import name.mlopatkin.andlogview.widgets.LinkOpener;

import net.miginfocom.swing.MigLayout;

import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

class LicensesUi extends JDialog {
    public LicensesUi(Window owner, OssComponents ossComponents) {
        super(owner, "List of third-party libraries used in " + Main.APP_NAME + " " + BuildInfo.VERSION,
                ModalityType.APPLICATION_MODAL);

        var content = getContentPane();
        // Max height is to prevent the dialog from growing too tall.
        content.setLayout(new MigLayout(
                LC().insets("dialog").wrapAfter(1).fillX().maxHeight("600lp"))
        );

        var thirdPartyComponents = new StringBuilder("""
                <html>
                <table>
                <tr>
                <th>Component</th>
                <th>Version</th>
                <th>License</th>
                </tr>
                """);

        for (var component : ossComponents.getComponents()) {
            appendComponent(thirdPartyComponents, component);
        }

        var text = new JEditorPane("text/html", thirdPartyComponents + """
                </table>
                </html>
                """);
        text.setEditable(false);
        text.addHyperlinkListener(new LinkOpener((url, failure) -> MyFutures.uncaughtException(failure)));

        var scrollPane = new JScrollPane(text);
        // Lame trick to always reserve some space for the scroll bar, so it doesn't cause content to wrap when it
        // appears.
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        content.add(scrollPane, CC().grow().wrap("push"));

        var okButton = new JButton("OK");
        okButton.addActionListener(e -> dispose());
        content.add(okButton, CC().alignX("right"));
        getRootPane().setDefaultButton(okButton);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(getParent());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    private StringBuilder appendComponent(StringBuilder builder, OssComponent component) {
        builder.append("<tr>");

        builder.append("<td>");
        builder.append("<a href=\"").append(component.getHomepage().toASCIIString()).append("\">");
        builder.append(component.getName());
        builder.append("</a>");
        builder.append("</td>");

        builder.append("<td>").append(component.getVersion()).append("</td>");

        builder.append("<td>");
        builder.append("<a href=\"andlogview://licenses/").append(component.getId()).append("\">");
        builder.append(component.getLicense());
        builder.append("</a>");
        builder.append("</td>");

        builder.append("</tr>");

        return builder;
    }
}

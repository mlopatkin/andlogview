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

import com.google.common.base.Preconditions;

import net.miginfocom.swing.MigLayout;

import org.jspecify.annotations.Nullable;

import java.awt.Container;
import java.awt.Window;
import java.util.Objects;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

class LicensesUi extends BaseAboutDialogUi {
    private final OssComponents ossComponents;
    private @Nullable JScrollPane scrollPane;

    public LicensesUi(Window owner, OssComponents ossComponents) {
        super(owner, "List of third-party libraries used in " + Main.APP_NAME + " " + BuildInfo.VERSION);
        this.ossComponents = ossComponents;

    }

    @Override
    protected MigLayout createContentLayout() {
        // Max height is to prevent the dialog from growing too tall.
        return new MigLayout(LC().insets("dialog").wrapAfter(1).fillX().maxHeight("600lp"));
    }

    @Override
    protected void createContent(Container content) {
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
        text.addHyperlinkListener(createBrowserLinkOpener());
        text.addHyperlinkListener(new AboutLinkHandler(this::onAboutLinkClick));
        text.setCaretPosition(0); // So the scroll doesn't go to the bottom.
        text.setFocusable(false); // Allow the default button to handle Enter press

        scrollPane = new JScrollPane(text);
        // Lame trick to always reserve some space for the scroll bar, so it doesn't cause content to wrap when it
        // appears.
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        content.add(scrollPane, lastComponentConstraint(CC().grow()));
    }

    @Override
    protected void createUi() {
        super.createUi();
        // After layout, we can revert scrollbars back to normal.
        Objects.requireNonNull(scrollPane).setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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

    private void onAboutLinkClick(String authority, String path) {
        if ("licenses".equals(authority)) {
            int id = parseId(path);
            new LicenseUi(this, ossComponents.getComponentById(id)).setVisible(true);
        }
    }

    private int parseId(String path) {
        Preconditions.checkArgument(path.startsWith("/"), "Unsupported path %s", path);
        return Integer.parseInt(path.substring(1));
    }
}

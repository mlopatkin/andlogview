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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Container;
import java.awt.Window;
import java.util.Objects;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class LicenseUi extends BaseAboutDialogUi {
    private final OssComponent ossComponent;
    private @Nullable JScrollPane scrollPane;

    public LicenseUi(Window owner, OssComponent ossComponent) {
        super(owner, "License for " + ossComponent.getName());
        this.ossComponent = ossComponent;
    }

    @Override
    protected MigLayout createContentLayout() {
        // Max height is to prevent the dialog from growing too tall.
        return new MigLayout(
                LC().insets("dialog").wrapAfter(1).fillX().maxHeight("600lp").width("600lp"));
    }

    @Override
    protected void createContent(Container content) {
        var scope = ossComponent.getScope();
        if (scope.contains("\n")) {
            scope = "\n" + scope;
        }
        var text = new JTextArea(
                "License for " + scope + "\n\n" + ossComponent.getLicenseText()
        );
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFocusable(false);  // Allow the default button to handle Enter press

        scrollPane = new JScrollPane(text);
        // Lame trick to always reserve some space for the scroll bar, so it doesn't cause content to wrap when it
        // appears.
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        content.add(scrollPane, lastComponentConstraint(CC().grow()));
    }

    @Override
    protected void createUi() {
        super.createUi();
        Objects.requireNonNull(scrollPane).setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }
}

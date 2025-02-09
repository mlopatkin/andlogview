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

import name.mlopatkin.andlogview.ErrorDialogsHelper;
import name.mlopatkin.andlogview.widgets.LinkOpener;

import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;

import java.awt.Container;
import java.awt.Window;
import java.io.IOException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

public abstract class BaseAboutDialogUi extends JDialog {
    private boolean uiReady;

    public BaseAboutDialogUi(Window parent, String dialogTitle) {
        super(parent, dialogTitle, ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible && !uiReady) {
            uiReady = true;
            createUi();
        }
        super.setVisible(visible);
    }

    protected void createUi() {
        var content = getContentPane();

        content.setLayout(createContentLayout());

        createContent(content);

        var okButton = new JButton("Close");
        okButton.addActionListener(e -> dispose());
        content.add(okButton, CC().alignX("right").spanX());
        getRootPane().setDefaultButton(okButton);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(getParent());
    }

    protected abstract MigLayout createContentLayout();

    protected abstract void createContent(Container content);

    protected CC lastComponentConstraint(CC constraint) {
        return constraint.wrap("related push");
    }

    protected LinkOpener createBrowserLinkOpener() {
        return new LinkOpener(this::onLinkOpeningFailed);
    }

    private void onLinkOpeningFailed(URL target, Exception failure) {
        if (failure instanceof IOException) {
            ErrorDialogsHelper.showError(this, "Cannot open the url %s in the default browser", target.toString());
        }
    }
}

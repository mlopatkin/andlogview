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

import java.awt.Window;
import java.util.Objects;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

public class ProgressDialog {
    private final JOptionPane optionPane;
    private final JDialog dialog;

    public ProgressDialog(Window owner, String title, String message, String actionTitle) {
        optionPane = new JOptionPane(
                new Object[] {
                        message,
                        createProgressBar()
                }, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, new String[] {actionTitle});

        dialog = optionPane.createDialog(owner, title);
    }

    public void show(Runnable cancellationAction) {
        dialog.setVisible(true);
        if (!Objects.equals(optionPane.getValue(), JOptionPane.UNINITIALIZED_VALUE)) {
            cancellationAction.run();
        }
    }

    public void hide() {
        dialog.setVisible(false);
        dialog.dispose();
    }

    private static JProgressBar createProgressBar() {
        var bar = new JProgressBar(JProgressBar.HORIZONTAL);
        bar.setIndeterminate(true);
        return bar;
    }
}

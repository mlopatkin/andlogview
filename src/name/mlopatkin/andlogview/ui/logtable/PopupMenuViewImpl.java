/*
 * Copyright 2020 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.logtable;

import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

/**
 * Implementation of {@link PopupMenuPresenter.PopupMenuView}.
 */
public class PopupMenuViewImpl implements PopupMenuPresenter.PopupMenuView {
    private final JComponent owner;
    private final int x;
    private final int y;

    protected final JPopupMenu popupMenu = new JPopupMenu();
    private final Action copyAction;

    public PopupMenuViewImpl(JComponent owner, int x, int y) {
        this.owner = owner;
        this.x = x;
        this.y = y;

        copyAction = UiHelper.createActionWrapper(owner, "copy", "Copy row", "control C");
    }

    @Override
    public void setCopyActionEnabled(boolean enabled) {
        copyAction.setEnabled(enabled);
        if (!isEmpty()) {
            popupMenu.addSeparator();
        }
        popupMenu.add(copyAction);
    }

    @Override
    public void show() {
        popupMenu.show(owner, x, y);
    }

    protected boolean isEmpty() {
        return popupMenu.getComponentCount() == 0;
    }
}

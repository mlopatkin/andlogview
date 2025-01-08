/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.widgets;

import com.google.common.base.Preconditions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

public class ActionBuilder {
    private final AbstractAction action;
    private boolean isCompleted;

    ActionBuilder(Runnable effect) {
        action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                effect.run();
            }
        };
    }

    public ActionBuilder name(String name) {
        action.putValue(Action.NAME, name);
        return this;
    }

    public ActionBuilder acceleratorKey(KeyStroke accelerator) {
        action.putValue(Action.ACCELERATOR_KEY, accelerator);
        return this;
    }

    public ActionBuilder smallIcon(Icon icon) {
        action.putValue(Action.SMALL_ICON, icon);
        return this;
    }

    public ActionBuilder largeIcon(Icon icon) {
        action.putValue(Action.LARGE_ICON_KEY, icon);
        return this;
    }

    public ActionBuilder shortDescription(String text) {
        action.putValue(Action.SHORT_DESCRIPTION, text);
        return this;
    }

    public ActionBuilder disabled() {
        action.setEnabled(false);
        return this;
    }

    public AbstractAction build() {
        Preconditions.checkState(!isCompleted, "Action is already built");
        isCompleted = true;
        return action;
    }
}

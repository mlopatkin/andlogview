/*
 * Copyright 2011 Mikhail Lopatkin
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
package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.apache.commons.lang3.StringEscapeUtils;

class UiHelper {

    private UiHelper() {
    }

    public static void addPopupMenu(final JComponent component, final JPopupMenu menu) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            private void showMenu(MouseEvent e) {
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    public static String covertToHtml(String value) {
        String escaped = StringEscapeUtils.escapeHtml4(value);
        String result = escaped.replace("\n", "<br>");
        return result;
    }

    public static String convertToSafe(String value) {
        String result = value.replace("\n", " ");
        return result;
    }

    public static void bindKeyFocused(JComponent component, String key, String actionKey,
            Action action) {
        component.getInputMap().put(KeyStroke.getKeyStroke(key), actionKey);
        component.getActionMap().put(actionKey, action);
    }

}
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Kind;

public class KindFilterMenu extends JPopupMenu {
    private static final Logger logger = Logger.getLogger(KindFilterMenu.class);

    private FilterController controller;

    private class KindCheckBoxMenuItem extends JCheckBoxMenuItem implements ActionListener {

        private LogRecord.Kind kind;

        public KindCheckBoxMenuItem(LogRecord.Kind kind, boolean selected) {
            super(kind.getCaption(), selected);
            addActionListener(this);
            this.kind = kind;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            controller.setBufferEnabled(kind, isSelected());
        }
    }

    public KindFilterMenu(EnumSet<Kind> enabledBuffers, FilterController controller) {
        this.controller = controller;
        for (Kind kind : enabledBuffers) {
            if (kind != LogRecord.Kind.UNKNOWN) {
                JMenuItem item = new KindCheckBoxMenuItem(kind, Configuration.ui
                        .bufferEnabled(kind));
                add(item);
            }
            logger.debug(kind);
        }
    }
}

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
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;

public class BufferFilterMenu extends JPopupMenu {
    private static final Logger logger = Logger.getLogger(BufferFilterMenu.class);

    private FilterController controller;

    private class BufferCheckBoxMenuItem extends JCheckBoxMenuItem implements ActionListener {

        private LogRecord.Buffer buffer;

        public BufferCheckBoxMenuItem(LogRecord.Buffer buffer, boolean selected) {
            super(buffer.getCaption(), selected);
            addActionListener(this);
            this.buffer = buffer;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            controller.setBufferEnabled(buffer, isSelected());
        }
    }

    public BufferFilterMenu(EnumSet<Buffer> enabledBuffers, FilterController controller) {
        this.controller = controller;
        for (Buffer buffer : enabledBuffers) {
            if (buffer != LogRecord.Buffer.UNKNOWN) {
                JMenuItem item = new BufferCheckBoxMenuItem(buffer, Configuration.ui
                        .bufferEnabled(buffer));
                add(item);
            }
            logger.debug(buffer);
        }
    }
}

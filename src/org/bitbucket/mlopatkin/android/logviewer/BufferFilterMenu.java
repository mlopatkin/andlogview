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
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map.Entry;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;

public class BufferFilterMenu {
    private FilterController controller;
    private JMenu parent;

    private class BufferCheckBoxMenuItem extends JCheckBoxMenuItem implements ActionListener {

        private Buffer buffer;

        public BufferCheckBoxMenuItem(Buffer buffer, boolean selected) {
            super(buffer.getCaption(), selected);
            addActionListener(this);
            this.buffer = buffer;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            controller.setBufferEnabled(buffer, isSelected());
        }
    }

    private EnumMap<Buffer, JMenuItem> items = new EnumMap<Buffer, JMenuItem>(Buffer.class);

    public BufferFilterMenu(JMenu parent, FilterController controller) {
        this.controller = controller;
        this.parent = parent;
        for (Buffer buffer : Buffer.values()) {
            if (buffer != Buffer.UNKNOWN) {
                JMenuItem item = new BufferCheckBoxMenuItem(buffer, Configuration.ui
                        .bufferEnabled(buffer));
                items.put(buffer, item);
                parent.add(item);
            }
        }
        resetBuffers();
    }

    private void resetBuffers() {
        for (Entry<Buffer, JMenuItem> entry : items.entrySet()) {
            controller.setBufferEnabled(entry.getKey(), false);
            entry.getValue().setVisible(false);
        }
        parent.setVisible(false);
    }

    public void setAvailableBuffers(EnumSet<Buffer> availableBuffers) {
        resetBuffers();
        boolean anyBufferAvailable = false;
        for (Buffer buffer : availableBuffers) {
            anyBufferAvailable = true;
            JMenuItem item = items.get(buffer);
            item.setVisible(true);
            controller.setBufferEnabled(buffer, item.isSelected());
        }
        parent.setVisible(anyBufferAvailable);
    }
}

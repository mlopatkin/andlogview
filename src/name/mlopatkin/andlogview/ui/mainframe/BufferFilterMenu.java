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
package name.mlopatkin.andlogview.ui.mainframe;

import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.filters.MainFilterController;
import name.mlopatkin.andlogview.liblogcat.LogRecord.Buffer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map.Entry;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public final class BufferFilterMenu {
    private final JMenu parent;
    private final MainFilterController controller;

    private final class BufferCheckBoxMenuItem extends JCheckBoxMenuItem implements ActionListener {
        private Buffer buffer;

        public BufferCheckBoxMenuItem(Buffer buffer, boolean selected) {
            super(buffer.getCaption(), selected);
            this.buffer = buffer;
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            controller.setBufferEnabled(buffer, isSelected());
        }
    }

    private EnumMap<Buffer, JMenuItem> items = new EnumMap<>(Buffer.class);

    public BufferFilterMenu(JMenu parent, MainFilterController controller) {
        this.parent = parent;
        this.controller = controller;
        for (Buffer buffer : Buffer.values()) {
            JMenuItem item = new BufferCheckBoxMenuItem(buffer, Configuration.ui.bufferEnabled(buffer));
            items.put(buffer, item);
            parent.add(item);
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
            assert item != null;
            item.setVisible(true);
            controller.setBufferEnabled(buffer, item.isSelected());
        }
        parent.setVisible(anyBufferAvailable);
    }
}

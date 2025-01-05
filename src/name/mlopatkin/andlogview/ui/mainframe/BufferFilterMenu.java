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

import name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;
import name.mlopatkin.andlogview.ui.filters.BufferFilterModel;

import java.util.EnumMap;
import java.util.EnumSet;

import javax.inject.Inject;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public final class BufferFilterMenu {
    private final BufferFilterModel bufferModel;
    private final JMenu parent = new JMenu("Buffers");
    private final EnumMap<Buffer, JMenuItem> items = new EnumMap<>(Buffer.class);

    @Inject
    public BufferFilterMenu(BufferFilterModel bufferModel) {
        this.bufferModel = bufferModel;

        for (Buffer buffer : Buffer.values()) {
            JMenuItem item = new JCheckBoxMenuItem(buffer.getCaption(), bufferModel.isBufferEnabled(buffer));
            item.addActionListener(e -> bufferModel.setBufferEnabled(buffer, item.isSelected()));
            items.put(buffer, item);
            parent.add(item);
        }

        parent.setVisible(false);
    }

    public JMenu getBuffersMenu() {
        return parent;
    }

    public void setAvailableBuffers(EnumSet<Buffer> availableBuffers) {
        if (availableBuffers.isEmpty()) {
            parent.setVisible(false);
            bufferModel.setBufferFilteringEnabled(false);
            return;
        }

        parent.setVisible(true);
        bufferModel.setBufferFilteringEnabled(true);

        items.forEach((buffer, menuItem) -> menuItem.setVisible(availableBuffers.contains(buffer)));
    }
}

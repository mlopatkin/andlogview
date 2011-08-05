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
import java.awt.event.MouseListener;

import javax.swing.JPanel;

class FilterPanel extends JPanel {

    private FilterController controller;

    public FilterPanel(FilterController controller) {
        this.controller = controller;
        addMouseListener(new FilterPanelClickListener());
    }

    private class FilterPanelClickListener extends MouseAdapter implements MouseListener {
        private static final int DOUBLE_CLICK_COUNT = 2;

        @Override
        public void mouseClicked(MouseEvent e) {
            // double click to add filter
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == DOUBLE_CLICK_COUNT) {
                controller.startFilterCreationDialog();
            }
        }
    }
}

/*
 * Copyright 2014 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel;

import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 * FilterPanel implementation. Generated class from WindowBuilder editor.
 */
class FilterPanelUi extends JPanel {

    private static final int SEPARATOR_HEIGHT = 42;
    private static final int SEPARATOR_WIDTH = 5;
    private static final int SCROLL_BUTTON_WIDTH = 26;

    protected final JButton btAddFilter;
    protected final JPanel content;
    protected final JButton btScrollLeft = new JButton();
    protected final JButton btScrollRight = new JButton();
    protected final JViewport contentViewport;

    FilterPanelUi() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        btAddFilter = new JButton();
        btAddFilter.setToolTipText("Add new filter");
        add(Box.createRigidArea(new Dimension(SEPARATOR_WIDTH, SEPARATOR_HEIGHT)));
        add(btAddFilter);
        add(Box.createRigidArea(new Dimension(SEPARATOR_WIDTH, SEPARATOR_HEIGHT)));
        add(btScrollLeft);
        add(Box.createRigidArea(new Dimension(SEPARATOR_WIDTH, SEPARATOR_HEIGHT)));
        content = new JPanel();
        content.setBorder(UiHelper.NO_BORDER);
        ((FlowLayout) content.getLayout()).setAlignment(FlowLayout.LEFT);
        JScrollPane scrollPane = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(UiHelper.NO_BORDER);
        add(scrollPane);
        add(Box.createRigidArea(new Dimension(SEPARATOR_WIDTH, SEPARATOR_HEIGHT)));
        add(btScrollRight);
        add(Box.createRigidArea(new Dimension(SEPARATOR_WIDTH, SEPARATOR_HEIGHT)));
        contentViewport = scrollPane.getViewport();

        UiHelper.setWidths(btScrollLeft, SCROLL_BUTTON_WIDTH);
        UiHelper.setWidths(btScrollRight, SCROLL_BUTTON_WIDTH);
    }
}

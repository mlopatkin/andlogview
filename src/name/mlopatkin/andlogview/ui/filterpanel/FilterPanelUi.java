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

package name.mlopatkin.andlogview.ui.filterpanel;

import name.mlopatkin.andlogview.widgets.UiHelper;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * FilterPanel implementation.
 */
class FilterPanelUi extends JPanel {
    private static final int SEPARATOR_WIDTH = 5;


    protected final JButton btAddFilter;
    protected final JPanel content;
    protected final JButton btScrollLeft = new JButton();
    protected final JSeparator sepScrollableLeft = createSeparator();
    protected final JSeparator sepScrollableRight = createSeparator();
    protected final JButton btScrollRight = new JButton();
    protected final JViewport contentViewport;

    FilterPanelUi(int separatorHeight) {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBackground(UIManager.getColor("ToolBar.background"));

        add(Box.createRigidArea(new Dimension(SEPARATOR_WIDTH, separatorHeight)));

        btAddFilter = new JButton();
        btAddFilter.setToolTipText("Add new filter");
        add(btAddFilter);

        add(Box.createRigidArea(new Dimension(SEPARATOR_WIDTH, separatorHeight)));

        add(btScrollLeft);

        add(Box.createRigidArea(new Dimension(SEPARATOR_WIDTH, separatorHeight)));

        add(sepScrollableLeft);

        add(Box.createRigidArea(new Dimension(SEPARATOR_WIDTH, separatorHeight)));

        content = new JPanel();
        content.setBackground(UIManager.getColor("ToolBar.background"));
        content.setBorder(UiHelper.NO_BORDER);
        JScrollPane scrollPane =
                new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(UiHelper.NO_BORDER);
        add(scrollPane);
        contentViewport = scrollPane.getViewport();

        add(Box.createRigidArea(new Dimension(SEPARATOR_WIDTH, separatorHeight)));

        add(sepScrollableRight);

        add(Box.createRigidArea(new Dimension(SEPARATOR_WIDTH, separatorHeight)));

        add(btScrollRight);

        add(Box.createRigidArea(new Dimension(SEPARATOR_WIDTH, separatorHeight)));
    }

    private JSeparator createSeparator() {
        JSeparator result = new JSeparator(SwingConstants.VERTICAL);
        result.setForeground(UIManager.getColor("Toolbar.separatorColor"));
        return result;
    }
}

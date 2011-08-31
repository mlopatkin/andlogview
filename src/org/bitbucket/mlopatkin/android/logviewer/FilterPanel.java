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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

import org.bitbucket.mlopatkin.android.liblogcat.filters.LogRecordFilter;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;

class FilterPanel extends JPanel {

    private FilterController controller;
    private Map<LogRecordFilter, FilterButton> buttons = new HashMap<LogRecordFilter, FilterButton>();

    public FilterPanel(FilterController controller) {
        this.controller = controller;
        controller.setPanel(this);
        addMouseListener(new FilterPanelClickListener());

        ((FlowLayout) getLayout()).setAlignment(FlowLayout.LEFT);

        JButton addFilter = new JButton(ADD_ICON);
        addFilter.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                FilterPanel.this.controller.startFilterCreationDialog();
            }
        });
        addFilter.setToolTipText("Add new filter");
        add(addFilter);
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

    public void addFilterButton(FilteringMode mode, LogRecordFilter filter) {
        FilterButton button = new FilterButton(mode, filter);
        add(button);
        menuHandler.addPopup(button);
        buttons.put(filter, button);
        validate();
    }

    public void removeFilterButton(LogRecordFilter filter) {
        FilterButton button = buttons.get(filter);
        buttons.remove(filter);
        if (button != null) {
            remove(button);
        }
        revalidate();
        repaint();
    }

    public JToggleButton getFilterButton(LogRecordFilter filter) {
        return buttons.get(filter);
    }

    private static URL getResource(String name) {
        return FilterPanel.class.getResource(name);
    }

    private static final ImageIcon FILTER_ICON = new ImageIcon(
            getResource("/icons/system-search.png"));
    private static final ImageIcon ADD_ICON = new ImageIcon(getResource("/icons/list-add.png"));

    private class FilterButton extends JToggleButton implements ActionListener {

        final LogRecordFilter filter;
        final FilteringMode mode;

        public FilterButton(FilteringMode mode, LogRecordFilter filter) {
            super(FILTER_ICON, true);
            this.filter = filter;
            this.mode = mode;
            addActionListener(this);
            setToolTipText("<html>" + mode.getDescription() + "<br>"
                    + UiHelper.covertToHtml(filter.toString()) + "</html>");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // controller.startEditFilterDialog(mode, filter);
            if (isSelected()) {
                controller.enableFilter(mode, filter);
            } else {
                controller.disableFilter(mode, filter);
            }
        }
    }

    private class PopupMenuHandler {
        private JPopupMenu menu = new JPopupMenu();
        private JMenuItem editItem = new JMenuItem("Edit filter");
        private JMenuItem removeItem = new JMenuItem("Remove filter");
        private FilterButton activeButton;

        PopupMenuHandler() {
            editItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    controller.startEditFilterDialog(activeButton.mode, activeButton.filter);
                }
            });

            removeItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    controller.removeFilter(activeButton.mode, activeButton.filter);
                }
            });
            menu.add(editItem);
            menu.add(removeItem);
        }

        void addPopup(final FilterButton button) {
            button.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showMenu(e);
                    }
                }

                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showMenu(e);
                    }
                }

                private void showMenu(MouseEvent e) {
                    activeButton = button;
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            });
        }
    }

    private PopupMenuHandler menuHandler = new PopupMenuHandler();
}

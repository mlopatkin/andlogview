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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;

import org.bitbucket.mlopatkin.android.liblogcat.filters.LogRecordFilter;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;

class FilterPanel extends JPanel {

    private FilterController controller;
    private Map<LogRecordFilter, FilterButton> buttons = new HashMap<LogRecordFilter, FilterButton>();

    private static final int SEPARATOR_HEIGHT = 42;
    private static final int SEPARATOR_WIDTH = 5;
    private static final int SCROLL_BUTTON_WIDTH = 26;

    public FilterPanel(FilterController controller) {
        this.controller = controller;
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        controller.setPanel(this);

        JButton addFilter = new JButton(acCreateFilter);
        addFilter.setToolTipText("Add new filter");
        add(Box.createRigidArea(new Dimension(SEPARATOR_WIDTH, SEPARATOR_HEIGHT)));
        add(addFilter);
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
        this.addComponentListener(resizeListener);
        UiHelper.addDoubleClickAction(this, acCreateFilter);
        UiHelper.addDoubleClickAction(content, acCreateFilter);
        UiHelper.setWidths(btScrollLeft, SCROLL_BUTTON_WIDTH);
        UiHelper.setWidths(btScrollRight, SCROLL_BUTTON_WIDTH);
    }

    public void addFilterButton(FilteringMode mode, LogRecordFilter filter) {
        FilterButton button = new FilterButton(mode, filter);
        content.add(button);
        menuHandler.addPopup(button);
        buttons.put(filter, button);
        validate();
    }

    public void removeFilterButton(LogRecordFilter filter) {
        FilterButton button = buttons.get(filter);
        buttons.remove(filter);
        if (button != null) {
            content.remove(button);
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
    private static final ImageIcon NEXT_ICON = new ImageIcon(getResource("/icons/go-next.png"));
    private static final ImageIcon PREV_ICON = new ImageIcon(getResource("/icons/go-previous.png"));

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

        @Override
        public String toString() {
            return filter.toString() + " " + super.toString();
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
    private JPanel content;
    private JViewport contentViewport;
    private int leftmostButton = -1;
    private int rightmostButton = -1;

    private void computeButtonIndices() {
        Rectangle viewportRect = contentViewport.getBounds();
        Rectangle contentsRect = content.getBounds();
        viewportRect.x -= contentsRect.x;
        viewportRect.y -= contentsRect.y;
        int cur = 0;
        leftmostButton = -1;
        rightmostButton = -1;
        for (Component button : content.getComponents()) {
            Rectangle buttonRect = button.getBounds();
            if (viewportRect.contains(buttonRect)) {
                if (leftmostButton < 0) {
                    leftmostButton = cur;
                }
                rightmostButton = cur;
            }
            ++cur;
        }
    }

    private void scrollTo(int i) {
        Component button = content.getComponent(i);
        content.scrollRectToVisible(button.getBounds());
        updateScrollState();
    }

    private Action acScrollLeft = new AbstractAction("", PREV_ICON) {
        @Override
        public void actionPerformed(ActionEvent e) {
            computeButtonIndices();
            if (leftmostButton > 0) {
                scrollTo(leftmostButton - 1);
            }
        }
    };

    private Action acScrollRight = new AbstractAction("", NEXT_ICON) {
        @Override
        public void actionPerformed(ActionEvent e) {
            computeButtonIndices();
            if (rightmostButton < content.getComponentCount() - 1) {
                scrollTo(rightmostButton + 1);
            }
        }
    };

    private JButton btScrollLeft = new JButton(acScrollLeft);
    private JButton btScrollRight = new JButton(acScrollRight);

    private void updateScrollState() {
        computeButtonIndices();
        boolean canScrollLeft = leftmostButton > 0;
        boolean canScrollRight = rightmostButton < content.getComponentCount() - 1;
        boolean canScroll = canScrollLeft | canScrollRight;

        acScrollLeft.setEnabled(canScrollLeft);
        acScrollRight.setEnabled(canScrollRight);

        btScrollRight.setVisible(canScroll);
        btScrollLeft.setVisible(canScroll);
    }

    private ComponentListener resizeListener = new ComponentAdapter() {
        public void componentResized(ComponentEvent e) {
            updateScrollState();
        };
    };

    private Action acCreateFilter = new AbstractAction("", ADD_ICON) {
        @Override
        public void actionPerformed(ActionEvent e) {
            controller.startFilterCreationDialog();
        }
    };
}

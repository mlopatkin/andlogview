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
package org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel;

import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;

import java.awt.Component;
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
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

public class FilterPanel extends FilterPanelUi implements FilterPanelModel.FilterPanelModelListener {

    private static final ImageIcon FILTER_ICON = new ImageIcon(
            getResource("/icons/system-search.png"));
    private static final ImageIcon ADD_ICON = new ImageIcon(getResource("/icons/list-add.png"));
    private static final ImageIcon NEXT_ICON = new ImageIcon(getResource("/icons/go-next.png"));
    private static final ImageIcon PREV_ICON = new ImageIcon(getResource("/icons/go-previous.png"));

    private final FilterPanelModel model;
    private final Map<PanelFilter, FilterButton> buttonByFilter = new HashMap<>();

    public FilterPanel(FilterPanelModel model) {
        this.model = model;

        model.addListener(this);

        btAddFilter.setAction(acCreateFilter);
        btScrollLeft.setAction(acScrollLeft);
        btScrollRight.setAction(acScrollRight);

        addComponentListener(resizeListener);

        UiHelper.addDoubleClickAction(this, acCreateFilter);
        UiHelper.addDoubleClickAction(content, acCreateFilter);
    }

    private static URL getResource(String name) {
        return FilterPanel.class.getResource(name);
    }

    @Override
    public void onFilterAdded(PanelFilter newFilter) {
        FilterButton button = new FilterButton(newFilter);
        buttonByFilter.put(newFilter, button);
        content.add(button);
        menuHandler.addPopup(button);
        validate();
    }

    @Override
    public void onFilterRemoved(PanelFilter filter) {
        FilterButton button = buttonByFilter.get(filter);
        buttonByFilter.remove(filter);
        if (button != null) {
            content.remove(button);
        }
        revalidate();
        repaint();
    }

    @Override
    public void onFilterReplaced(PanelFilter oldFilter, PanelFilter newFilter) {

    }


    private class FilterButton extends JToggleButton implements ActionListener {
        private PanelFilter filter;

        public FilterButton(PanelFilter filter) {
            super(FILTER_ICON, true);

            addActionListener(this);
            setFilter(filter);
        }

        public PanelFilter getFilter() {
            return filter;
        }

        public void setFilter(PanelFilter newFilter) {
            filter = newFilter;
            filter.setEnabled(isSelected());
            setToolTipText(filter.getTooltip());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            filter.setEnabled(isSelected());
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
                    activeButton.filter.openFilterEditor();
                }
            });

            removeItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    model.removeFilter(activeButton.getFilter());
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
        }

        ;
    };

    private Action acCreateFilter = new AbstractAction("", ADD_ICON) {
        @Override
        public void actionPerformed(ActionEvent e) {
//            controller.startFilterCreationDialog();
        }
    };
}

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
package org.bitbucket.mlopatkin.android.logviewer.ui.bookmarks;

import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.config.Configuration;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexframe.Dagger_IndexFrameComponent;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexframe.IndexController;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexframe.IndexFrameComponent;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexframe.IndexFrameModule;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.MainFrameDependencies;
import org.bitbucket.mlopatkin.android.logviewer.widgets.DecoratingCellRenderer;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

@Singleton
public class BookmarkController implements IndexController {

    private JTable table;

    private TableRowSorter<LogRecordTableModel> rowSorter;

    @Inject
    public BookmarkController(MainFrameDependencies mainFrameDependencies, BookmarkModel bookmarksModel) {
        IndexFrameComponent indexFrame = Dagger_IndexFrameComponent.builder()
                                                                   .mainFrameDependencies(mainFrameDependencies)
                                                                   .indexFrameModule(new IndexFrameModule(this))
                                                                   .build();
        indexFrame.createFrame();

//        getFrame().setTitle("Bookmarks");

//        table = getFrame().getTable();
//        rowSorter = new SortingDisableSorter<LogRecordTableModel>(model);
//        table.setRowSorter(rowSorter);
//        table.getSelectionModel().addListSelectionListener(this);
//
//        rowSorter.setRowFilter(RowFilter.andFilter(Arrays.asList(filter, showHideFilter)));
//        mainTable.addDecorator(new BookmarksHighlighter());
//        setupPopupMenu();
    }


    private void setupPopupMenu() {
//        TablePopupMenu menu = getFrame().getPopupMenu();
//        menu.addItemsUpdater(this);
//        menu.add(acDeleteBookmarks);
        UiHelper.bindKeyFocused(table, "DELETE", "remove_bookmark", acDeleteBookmarks);
        acDeleteBookmarks.setEnabled(table.getSelectedRowCount() > 0);
    }

//
//    @Override
//    public void showWindow() {
//        getFrame().setVisible(true);
//    }
//
//    @Override
//    protected void onMainTableUpdate() {
//        update();
//    }

    public void clear() {

    }

    private void update() {
//        getMainTable().repaint();
    }

    @Override
    public void activateRow(int row) {
        // TODO
    }

    @Override
    public void onWindowClosed() {
        // TODO
    }

    private class BookmarksHighlighter implements DecoratingCellRenderer {

        private TableCellRenderer inner;

        @Override
        public void setInnerRenderer(TableCellRenderer renderer) {
            inner = renderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component cmp = inner.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
            int modelRow = table.convertRowIndexToModel(row);
//            LogRecord record = getModel().getRowData(modelRow);
//            if (filter.include(modelRow, record)) {
//                highlight(cmp, isSelected);
//            }

            return cmp;
        }

        private void highlight(Component cmp, boolean isSelected) {
            Color backgroundColor = Configuration.ui.bookmarkBackground();
            Color foregroundColor = Configuration.ui.bookmarkedForeground();
            if (isSelected) {
                backgroundColor = backgroundColor.brighter();
            }
            if (backgroundColor != null) {
                cmp.setBackground(backgroundColor);
            }
            if (foregroundColor != null) {
                cmp.setForeground(foregroundColor);
            }
        }

    }

    private Action acDeleteBookmarks = new AbstractAction("Remove from bookmarks") {
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };

//    @Override
//    public void updateItemsState(JTable source) {
//        acDeleteBookmarks.setEnabled(source.getSelectedRowCount() > 0);
//    }
//
//    @Override
//    public void valueChanged(ListSelectionEvent e) {
//        if (!e.getValueIsAdjusting()) {
//            updateItemsState(table);
//        }
//    }
//
//    public boolean isMarked(int row) {
//        return filter.include(row, getModel().getRowData(row));
//    }
}

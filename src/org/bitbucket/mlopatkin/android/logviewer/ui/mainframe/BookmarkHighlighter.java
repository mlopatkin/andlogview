/*
 * Copyright 2015 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.mainframe;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.config.Configuration;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableModel;
import org.bitbucket.mlopatkin.android.logviewer.widgets.DecoratingCellRenderer;

import java.awt.Color;
import java.awt.Component;

import javax.inject.Inject;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class BookmarkHighlighter implements DecoratingCellRenderer {

    private final LogRecordTableModel model;
    private final BookmarkModel bookmarkModel;
    private TableCellRenderer renderer;

    @Inject
    public BookmarkHighlighter(LogRecordTableModel model, BookmarkModel bookmarkModel) {
        this.model = model;
        this.bookmarkModel = bookmarkModel;
    }

    @Override
    public void setInnerRenderer(TableCellRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        Component innerRenderer =
                renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        LogRecord record = model.getRowData(table.convertRowIndexToModel(row));
        if (bookmarkModel.containsRecord(record)) {
            highlight(innerRenderer, isSelected);
        }
        return innerRenderer;
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

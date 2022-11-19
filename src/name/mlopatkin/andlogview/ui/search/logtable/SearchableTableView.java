/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.search.logtable;

import name.mlopatkin.andlogview.search.SearchModel;
import name.mlopatkin.andlogview.search.logrecord.RowSearchStrategy;
import name.mlopatkin.andlogview.ui.search.SearchPresenter;
import name.mlopatkin.andlogview.widgets.DecoratingRendererTable;

import java.util.Optional;

import javax.swing.JTable;

public class SearchableTableView
        implements SearchPresenter.SearchableView<TablePosition>, SearchModel.StrategyObserver<RowSearchStrategy> {
    private final JTable table;
    private final SearchResultsHighlightCellRenderer searchHighlightRenderer = new SearchResultsHighlightCellRenderer();

    public SearchableTableView(DecoratingRendererTable table) {
        this.table = table;
        table.addDecorator(searchHighlightRenderer);
    }

    @Override
    public void showSearchResult(TablePosition row) {
        int curRow = row.getViewIndex(table);
        table.getSelectionModel().setSelectionInterval(curRow, curRow);
        table.scrollRectToVisible(table.getCellRect(curRow, 0, false));
        table.requestFocusInWindow();
    }

    @Override
    public Optional<TablePosition> getSearchStartPosition() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            selectedRow = table.rowAtPoint(table.getVisibleRect().getLocation());
        }
        if (selectedRow >= 0) {
            return Optional.of(TablePosition.fromViewIndex(table, selectedRow));
        }
        return Optional.empty();
    }

    @Override
    public void onNewSearchStrategy(RowSearchStrategy searchStrategy) {
        searchHighlightRenderer.setHighlightStrategy(searchStrategy);
        table.repaint();
    }

    @Override
    public void onSearchStrategyCleared() {
        searchHighlightRenderer.setHighlightStrategy(null);
        table.repaint();
    }
}

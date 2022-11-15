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

package name.mlopatkin.andlogview.ui.mainframe.search;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.search.RowSearchStrategy;
import name.mlopatkin.andlogview.search.SearchModel;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableModel;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameDependencies;
import name.mlopatkin.andlogview.ui.search.SearchPresenter;
import name.mlopatkin.andlogview.ui.search.SearchScoped;
import name.mlopatkin.andlogview.ui.search.logtable.LogTableSearchAdapter;
import name.mlopatkin.andlogview.ui.search.logtable.SearchableTableView;
import name.mlopatkin.andlogview.ui.search.logtable.TablePosition;
import name.mlopatkin.andlogview.widgets.DecoratingRendererTable;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.swing.JTable;

@Module
public abstract class MainFrameSearchModule {
    @Provides
    @SearchScoped
    static SearchPresenter.SearchableView<TablePosition> getSearchableView(
            @Named(MainFrameDependencies.FOR_MAIN_FRAME) JTable logTable,
            SearchModel<LogRecord, TablePosition, RowSearchStrategy> searchModel) {
        // TODO(mlopatkin) Replace this cast with injection
        var tableView = new SearchableTableView(((DecoratingRendererTable) logTable));
        searchModel.asSearchStrategyObservable().addObserver(tableView);
        return tableView;
    }

    @Provides
    @SearchScoped
    static LogTableSearchAdapter getSearchAdapter(@Named(MainFrameDependencies.FOR_MAIN_FRAME) JTable logTable,
            LogRecordTableModel tableModel) {
        return new LogTableSearchAdapter(logTable, tableModel);
    }

    @Binds
    abstract SearchPresenter.SearchPromptView getSearchPromptView(MainFrameSearchPromptView impl);
}

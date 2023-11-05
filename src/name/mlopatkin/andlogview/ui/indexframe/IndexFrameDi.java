/*
 * Copyright 2020 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.ui.indexframe;

import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.ColumnOrder;
import name.mlopatkin.andlogview.ui.logtable.LogModelFilter;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableColumnModel;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableModel;
import name.mlopatkin.andlogview.ui.logtable.LogTableModule;
import name.mlopatkin.andlogview.ui.logtable.LogTableScoped;
import name.mlopatkin.andlogview.ui.logtable.PopupMenu;
import name.mlopatkin.andlogview.ui.logtable.PopupMenuPresenter;
import name.mlopatkin.andlogview.ui.logtable.PopupMenuViewImpl;
import name.mlopatkin.andlogview.ui.logtable.TableRow;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameDependencies;

import com.google.common.collect.ImmutableList;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;

import javax.inject.Named;
import javax.swing.JTable;

public final class IndexFrameDi {
    public static final String FOR_INDEX_FRAME = "Index frame";

    private IndexFrameDi() {}

    @Component(dependencies = MainFrameDependencies.class, modules = {TableColumnsModule.class, TableModule.class})
    @IndexFrameScoped
    public interface IndexFrameComponent {
        IndexFrame createFrame();

        @Component.Builder
        interface Builder {
            Builder mainFrameDependencies(MainFrameDependencies deps);

            @BindsInstance
            Builder setIndexController(IndexController indexController);

            @BindsInstance
            Builder setIndexFilter(@Named(FOR_INDEX_FRAME) LogModelFilter logModelFilter);

            IndexFrameComponent build();
        }
    }

    @Module
    public static class TableColumnsModule {
        private static final ImmutableList<Column> INDEX_FRAME_COLUMNS =
                ImmutableList.of(Column.INDEX, Column.TIME, Column.PID, Column.PRIORITY, Column.TAG, Column.MESSAGE);

        @Provides
        static LogRecordTableColumnModel getColumnModel() {
            return new LogRecordTableColumnModel(
                    null, INDEX_FRAME_COLUMNS, ColumnOrder.canonical(), new HashSet<>(INDEX_FRAME_COLUMNS));
        }
    }

    @Module
    static class TableModule {
        @Provides
        @IndexFrameScoped
        @Named(FOR_INDEX_FRAME)
        static JTable getIndexWindowTable(LogRecordTableModel model,
                @Named(FOR_INDEX_FRAME) LogModelFilter logModelFilter) {
            return DaggerIndexFrameDi_IndexLogTableComponent.factory().create(model, logModelFilter).getLogTable();
        }
    }

    @LogTableScoped
    @Component(modules = {LogTableModule.class, TableDepsModule.class})
    interface IndexLogTableComponent {
        JTable getLogTable();

        @Component.Factory
        interface Factory {
            @SuppressWarnings("ClassEscapesDefinedScope")
            IndexLogTableComponent create(@BindsInstance LogRecordTableModel tableModel,
                    @BindsInstance LogModelFilter modelFilter);
        }
    }

    @Module
    static class TableDepsModule {
        @Provides
        static PopupMenu.Delegate createPopupMenuDelegate(
                PopupMenuPresenter<PopupMenuPresenter.PopupMenuView> presenter) {
            return new PopupMenu.Delegate() {
                @Override
                public void showMenu(JTable table, int x, int y, Column column, @Nullable TableRow row) {
                    PopupMenuPresenter.PopupMenuView view = new PopupMenuViewImpl(table, x, y);
                    presenter.showContextMenu(view, column, row);
                }
            };
        }
    }
}

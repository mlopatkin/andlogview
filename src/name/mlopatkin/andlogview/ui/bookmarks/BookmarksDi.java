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

package name.mlopatkin.andlogview.ui.bookmarks;

import static name.mlopatkin.andlogview.ui.indexframe.IndexFrameDi.FOR_INDEX_FRAME;

import name.mlopatkin.andlogview.bookmarks.BookmarkModel;
import name.mlopatkin.andlogview.ui.indexframe.IndexFrameDi;
import name.mlopatkin.andlogview.ui.indexframe.IndexFrameScoped;
import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.LogModelFilter;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableModel;
import name.mlopatkin.andlogview.ui.logtable.LogTableModule;
import name.mlopatkin.andlogview.ui.logtable.LogTableScoped;
import name.mlopatkin.andlogview.ui.logtable.PopupMenu;
import name.mlopatkin.andlogview.ui.logtable.TableRow;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameDependencies;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Named;
import javax.swing.JTable;


final class BookmarksDi {
    @IndexFrameScoped
    @Component(dependencies = MainFrameDependencies.class,
            modules = {IndexFrameDi.TableColumnsModule.class, TableModule.class})
    interface BookmarksFrameComponent extends IndexFrameDi.IndexFrameComponent {
        @Component.Builder
        interface Builder extends IndexFrameDi.IndexFrameComponent.Builder {
            @Override
            @SuppressWarnings("ClassEscapesDefinedScope")
            BookmarksFrameComponent build();
        }

        DeleteBookmarksAction createDeleteBookmarksAction();
    }

    @Module
    static class TableModule {
        @Provides
        @IndexFrameScoped
        static BookmarksLogTableComponent getLogTableComponent(
                LogRecordTableModel tableModel,
                @Named(FOR_INDEX_FRAME) LogModelFilter filter,
                BookmarkModel bookmarkModel) {
            return DaggerBookmarksDi_BookmarksLogTableComponent.factory()
                    .create(tableModel, filter, bookmarkModel);
        }

        @Provides
        @Named(FOR_INDEX_FRAME)
        static JTable createLogTable(BookmarksLogTableComponent logTableComponent) {
            return logTableComponent.getLogTable();
        }

        @Provides
        static DeleteBookmarksAction createDeleteBookmarksAction(BookmarksLogTableComponent logTableComponent) {
            return logTableComponent.createDeleteBookmarksAction();
        }
    }

    @LogTableScoped
    @Component(modules = {LogTableModule.class, TableDepsModule.class})
    interface BookmarksLogTableComponent {
        JTable getLogTable();

        DeleteBookmarksAction createDeleteBookmarksAction();

        @Component.Factory
        interface Factory {
            @SuppressWarnings("ClassEscapesDefinedScope")
            BookmarksLogTableComponent create(@BindsInstance LogRecordTableModel tableModel,
                    @BindsInstance LogModelFilter modelFilter, @BindsInstance BookmarkModel bookmarkModel);
        }
    }

    @Module
    static class TableDepsModule {
        @SuppressWarnings("Convert2Lambda")
        @Provides
        static PopupMenu.Delegate createDelegate(BookmarkPopupMenuPresenter presenter) {
            return new PopupMenu.Delegate() {
                @Override
                public void showMenu(JTable table, int x, int y, Column column, @Nullable TableRow row) {
                    BookmarkPopupMenuPresenter.BookmarkPopupMenuView view = new BookmarkPopupMenuViewImpl(table, x, y);
                    presenter.showContextMenu(view, column, row);
                }
            };
        }
    }

}

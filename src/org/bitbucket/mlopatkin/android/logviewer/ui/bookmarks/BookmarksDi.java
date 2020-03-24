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

package org.bitbucket.mlopatkin.android.logviewer.ui.bookmarks;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexframe.IndexFrameDi;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexframe.IndexFrameScoped;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogModelFilter;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogTableModule;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogTableScoped;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.PopupMenu;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TableRow;
import org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.MainFrameDependencies;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Named;
import javax.swing.JTable;

import static org.bitbucket.mlopatkin.android.logviewer.ui.indexframe.IndexFrameDi.FOR_INDEX_FRAME;


final class BookmarksDi {
    @IndexFrameScoped
    @Component(dependencies = MainFrameDependencies.class,
            modules = {IndexFrameDi.TableColumnsModule.class, TableModule.class})
    interface BookmarksFrameComponent extends IndexFrameDi.IndexFrameComponent {
        @Component.Builder
        interface Builder extends IndexFrameDi.IndexFrameComponent.Builder {
            @Override
            BookmarksFrameComponent build();
        }
    }

    @Module
    static class TableModule {
        @Provides
        @Named(FOR_INDEX_FRAME)
        static JTable createLogTable(LogRecordTableModel tableModel, @Named(FOR_INDEX_FRAME) LogModelFilter filter,
                BookmarkModel bookmarkModel) {
            return DaggerBookmarksDi_BookmarksLogTableComponent.factory()
                    .create(tableModel, filter, bookmarkModel)
                    .getLogTable();
        }
    }

    @LogTableScoped
    @Component(modules = {LogTableModule.class, TableDepsModule.class})
    interface BookmarksLogTableComponent {
        JTable getLogTable();

        @Component.Factory
        interface Factory {
            BookmarksLogTableComponent create(@BindsInstance LogRecordTableModel tableModel,
                    @BindsInstance LogModelFilter modelFilter, @BindsInstance BookmarkModel bookmarkModel);
        }
    }

    @Module
    static class TableDepsModule {
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

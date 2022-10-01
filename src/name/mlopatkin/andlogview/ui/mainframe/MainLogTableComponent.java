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

package name.mlopatkin.andlogview.ui.mainframe;

import name.mlopatkin.andlogview.bookmarks.BookmarkModel;
import name.mlopatkin.andlogview.ui.logtable.LogModelFilter;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableModel;
import name.mlopatkin.andlogview.ui.logtable.LogTableModule;
import name.mlopatkin.andlogview.ui.logtable.LogTableScoped;
import name.mlopatkin.andlogview.ui.mainframe.popupmenu.MenuFilterCreator;

import dagger.BindsInstance;
import dagger.Component;

import javax.swing.JTable;

@LogTableScoped
@Component(modules = {LogTableModule.class, MainLogTableModule.class})
public interface MainLogTableComponent {
    JTable getLogTable();

    BookmarkHighlighter getBookmarkHighlighter();

    @Component.Factory
    interface Factory {
        MainLogTableComponent create(
                @BindsInstance LogRecordTableModel tableModel, @BindsInstance LogModelFilter modelFilter,
                @BindsInstance BookmarkModel bookmarkModel, @BindsInstance MenuFilterCreator filterCreator,
                @BindsInstance DialogFactory dialogFactory);
    }
}

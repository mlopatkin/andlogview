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

package name.mlopatkin.andlogview.ui.mainframe;

import name.mlopatkin.andlogview.AppGlobals;
import name.mlopatkin.andlogview.MainFrame;
import name.mlopatkin.andlogview.bookmarks.BookmarkModel;
import name.mlopatkin.andlogview.ui.device.AdbServicesSubcomponent;
import name.mlopatkin.andlogview.ui.logtable.LogModelFilter;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableModel;
import name.mlopatkin.andlogview.ui.search.SearchScoped;
import name.mlopatkin.andlogview.ui.search.logtable.LogTableSearchModule;

import dagger.Component;

import javax.inject.Named;
import javax.swing.JTable;

/**
 * Bootstrap class to retrieve dependencies of the Main frame during transitional period.
 */
@MainFrameScoped
@SearchScoped
@Component(
        modules = {
                AdbServicesSubcomponent.AdbMainModule.class,
                MainFrameModule.class,
                LogTableSearchModule.class,
        },
        dependencies = AppGlobals.class)
public interface MainFrameDependencies {
    String FOR_MAIN_FRAME = "Main frame";

    // Used in Bookmarks and IndexFrame
    @Named(FOR_MAIN_FRAME)
    JTable getLogTable();

    // Used in Bookmarks and IndexFrame
    LogRecordTableModel getLogModel();

    // Used in IndexFrame
    LogModelFilter getFilter();

    // Used in Bookmarks and IndexFrame
    DialogFactory getDialogFactory();

    // Used in BookmarksFrameComponent
    BookmarkModel getBookmarkModel();

    void injectMainFrame(MainFrame frame);

    @Component.Factory
    interface Factory {
        MainFrameDependencies create(MainFrameModule mainFrameModule, AppGlobals globals);
    }
}

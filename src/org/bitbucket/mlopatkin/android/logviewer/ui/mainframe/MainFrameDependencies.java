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

import dagger.Component;

import org.bitbucket.mlopatkin.android.logviewer.DataSourceHolder;
import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.filters.MainFilterController;
import org.bitbucket.mlopatkin.android.logviewer.ui.bookmarks.BookmarkController;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.FilterPanel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogModelFilter;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogTable;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Bootstrap class to retrieve dependencies of the Main frame during transitional period.
 */
@Singleton
@Component(modules = MainFrameModule.class)
public interface MainFrameDependencies {
    String FOR_MAIN_FRAME = "Main frame";

    @Named(FOR_MAIN_FRAME)
    LogTable getLogTable();

    FilterPanel getFilterPanel();

    LogRecordTableModel getLogModel();

    LogModelFilter getFilter();

    DialogFactory getDialogFactory();

    LogRecordPopupMenuHandler.Factory getPopupMenuHandlerFactory();

    BookmarkModel getBookmarkModel();

    BookmarkController getBookmarkController();

    MainFilterController getMainFilterController();

    DataSourceHolder getDataSourceHolder();
}

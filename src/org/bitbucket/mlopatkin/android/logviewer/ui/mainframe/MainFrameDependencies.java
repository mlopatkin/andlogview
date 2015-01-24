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

import org.bitbucket.mlopatkin.android.logviewer.filters.MainFilterController;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.FilterPanel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogTable;

import dagger.Component;

import javax.inject.Singleton;

/**
 * Bootstrap class to retrieve dependencies of the Main frame during transitional period.
 */
@Singleton
@Component(modules = MainFrameModule.class)
public interface MainFrameDependencies {
    MainFilterController getFilterController();
    LogTable getLogTable();
    FilterPanel getFilterPanel();
    LogRecordTableModel getLogModel();
}

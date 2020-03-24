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

package org.bitbucket.mlopatkin.android.logviewer.ui.mainframe;

import dagger.BindsInstance;
import dagger.Component;

import org.bitbucket.mlopatkin.android.logviewer.bookmarks.BookmarkModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogModelFilter;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogTableModule;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogTableScoped;

import javax.swing.JTable;

@LogTableScoped
@Component(modules = {LogTableModule.class, MainLogTableModule.class})
public interface MainLogTableComponent {
    JTable getLogTable();

    @Component.Factory
    interface Factory {
        MainLogTableComponent create(
                @BindsInstance LogRecordTableModel tableModel, @BindsInstance LogModelFilter modelFilter,
                @BindsInstance BookmarkModel bookmarkModel);
    }
}

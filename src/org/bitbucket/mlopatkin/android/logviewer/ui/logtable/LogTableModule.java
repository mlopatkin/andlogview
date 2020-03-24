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

package org.bitbucket.mlopatkin.android.logviewer.ui.logtable;

import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;

import javax.swing.JTable;

@Module(includes = LogTableModule.Bindings.class)
public class LogTableModule {
    @LogTableScoped
    @Provides
    JTable initializeLogTable(LogTableInitializer tableFactory) {
        return tableFactory.completeInitialization();
    }

    @LogTableScoped
    @Provides
    LogTable createBaseLogTable(LogRecordTableModel model, LogModelFilter modelFilter) {
        return LogTable.create(model, modelFilter);
    }

    @Module
    abstract static class Bindings {
        @Binds
        @LogTableScoped
        abstract SelectedRows bindSelectedRows(SelectedRowsImpl impl);

        @BindsOptionalOf
        abstract PopupMenu.Delegate bindPopupMenuDelegate();
    }
}

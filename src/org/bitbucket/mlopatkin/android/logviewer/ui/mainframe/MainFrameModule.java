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

import dagger.Module;
import dagger.Provides;

import org.bitbucket.mlopatkin.android.logviewer.MainFrame;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilterModule;
import org.bitbucket.mlopatkin.android.logviewer.filters.MainFilterController;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.FilterCreator;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogModelFilter;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogTable;

import javax.inject.Named;
import javax.swing.JFrame;

@Module(includes = {FilterModule.class, MainFramePrefsModule.class})
public class MainFrameModule {
    private final MainFrame mainFrame;

    public MainFrameModule(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Provides
    @MainFrameScoped
    DialogFactory provideDialogFactory() {
        return new DialogFactory() {
            @Override
            public JFrame getOwner() {
                return mainFrame;
            }
        };
    }

    @Provides
    FilterCreator provideFilterCreator(MainFilterController mainFilterController) {
        return mainFilterController;
    }

    @Provides
    @MainFrameScoped
    @Named(MainFrameDependencies.FOR_MAIN_FRAME)
    LogTable getMainLogTable(LogRecordTableModel model,
                             LogModelFilter filter,
                             BookmarkHighlighter bookmarkHighlighter) {
        LogTable logTable = LogTable.create(model, filter);
        logTable.addDecorator(bookmarkHighlighter);
        return logTable;
    }
}

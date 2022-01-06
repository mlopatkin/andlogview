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

import name.mlopatkin.andlogview.MainFrame;
import name.mlopatkin.andlogview.bookmarks.BookmarkModel;
import name.mlopatkin.andlogview.filters.FilterModule;
import name.mlopatkin.andlogview.filters.MainFilterController;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filterpanel.FilterCreator;
import name.mlopatkin.andlogview.ui.logtable.LogModelFilter;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableModel;
import name.mlopatkin.andlogview.ui.mainframe.popupmenu.MenuFilterCreator;
import name.mlopatkin.andlogview.ui.preferences.PreferencesUiModule;
import name.mlopatkin.andlogview.ui.status.StatusPanelModule;
import name.mlopatkin.andlogview.widgets.DecoratingRendererTable;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.swing.JFrame;
import javax.swing.JTable;

@Module(includes = {FilterModule.class, MainFramePrefsModule.class, StatusPanelModule.class, PreferencesUiModule.class})
public class MainFrameModule {
    private final MainFrame mainFrame;

    public MainFrameModule(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    // TODO(mlopatkin) get rid of this
    @Provides
    MainFrame getMainFrame() {
        return mainFrame;
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
    JTable getMainLogTable(LogRecordTableModel model, LogModelFilter filter, BookmarkHighlighter bookmarkHighlighter,
            BookmarkModel bookmarkModel, Lazy<MainFilterController> filterController, DialogFactory dialogFactory) {
        JTable logTable = DaggerMainLogTableComponent.factory()
                .create(model, filter, bookmarkModel, new MenuFilterCreator() {
                    // TODO(mlopatkin) break dependency cycle and replace with direct dependency (?)
                    @Override
                    public void addFilter(FilterFromDialog filter) {
                        filterController.get().addFilter(filter);
                    }

                    @Override
                    public void createFilterWithDialog(FilterFromDialog baseData) {
                        filterController.get().createFilterWithDialog(baseData);
                    }
                }, dialogFactory).getLogTable();
        // TODO(mlopatkin) Replace this cast with injection
        ((DecoratingRendererTable) logTable).addDecorator(bookmarkHighlighter);
        return logTable;
    }
}

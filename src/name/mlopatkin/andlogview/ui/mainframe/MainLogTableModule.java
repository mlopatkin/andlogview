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

import dagger.Module;
import dagger.Provides;

import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.PopupMenu;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.TableRow;
import org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.popupmenu.TablePopupMenuPresenter;
import org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.popupmenu.TablePopupMenuViewImpl;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.JTable;

@Module
class MainLogTableModule {
    @Provides
    PopupMenu.Delegate providePopupMenu(TablePopupMenuPresenter presenter) {
        return new PopupMenu.Delegate() {
            @Override
            public void showMenu(JTable table, int x, int y, Column column, @Nullable TableRow row) {
                TablePopupMenuViewImpl menuView = new TablePopupMenuViewImpl(table, x, y);
                presenter.showContextMenu(menuView, column, row);
            }
        };
    }
}

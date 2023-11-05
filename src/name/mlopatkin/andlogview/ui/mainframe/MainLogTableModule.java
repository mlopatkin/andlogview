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

import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.PopupMenu;
import name.mlopatkin.andlogview.ui.logtable.TableRow;
import name.mlopatkin.andlogview.ui.mainframe.popupmenu.TablePopupMenuPresenter;
import name.mlopatkin.andlogview.ui.mainframe.popupmenu.TablePopupMenuViewImpl;

import dagger.Module;
import dagger.Provides;

import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.JTable;

@Module
class MainLogTableModule {
    @SuppressWarnings("Convert2Lambda")
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

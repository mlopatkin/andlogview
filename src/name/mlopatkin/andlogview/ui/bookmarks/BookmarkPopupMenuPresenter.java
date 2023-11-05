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

package name.mlopatkin.andlogview.ui.bookmarks;

import name.mlopatkin.andlogview.bookmarks.BookmarkModel;
import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.PopupMenuPresenter;
import name.mlopatkin.andlogview.ui.logtable.SelectedRows;
import name.mlopatkin.andlogview.ui.logtable.TableRow;
import name.mlopatkin.andlogview.utils.events.Observable;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

import javax.inject.Inject;

public class BookmarkPopupMenuPresenter extends PopupMenuPresenter<BookmarkPopupMenuPresenter.BookmarkPopupMenuView> {
    protected interface BookmarkPopupMenuView extends PopupMenuPresenter.PopupMenuView {

        Observable<Runnable> setDeleteBookmarksActionEnabled(boolean enabled);
    }

    private final BookmarkModel bookmarkModel;

    @Inject
    public BookmarkPopupMenuPresenter(SelectedRows selectedRows, BookmarkModel bookmarkModel) {
        super(selectedRows);
        this.bookmarkModel = bookmarkModel;
    }

    @Override
    protected void configureMenu(BookmarkPopupMenuView view, Column c, @Nullable TableRow row,
            List<TableRow> selection) {
        super.configureMenu(view, c, row, selection);
        view.setDeleteBookmarksActionEnabled(!selection.isEmpty()).addObserver(() -> removeSelectedRecords(selection));
    }

    private void removeSelectedRecords(List<TableRow> selection) {
        for (TableRow tableRow : selection) {
            bookmarkModel.removeRecord(tableRow.getRecord());
        }
    }
}

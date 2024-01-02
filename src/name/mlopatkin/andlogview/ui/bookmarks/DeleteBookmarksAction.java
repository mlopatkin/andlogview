/*
 * Copyright 2024 the Andlogview authors
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
import name.mlopatkin.andlogview.ui.logtable.SelectedRows;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;

class DeleteBookmarksAction implements ActionListener {
    private final BookmarkModel bookmarkModel;
    private final SelectedRows selectedRows;

    @Inject
    public DeleteBookmarksAction(BookmarkModel bookmarkModel, SelectedRows selectedRows) {
        this.bookmarkModel = bookmarkModel;
        this.selectedRows = selectedRows;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        var rowsToDelete = selectedRows.getSelectedRows();
        for (var tableRow : rowsToDelete) {
            bookmarkModel.removeRecord(tableRow.getRecord());
        }
    }
}

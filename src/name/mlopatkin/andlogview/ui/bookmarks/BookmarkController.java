/*
 * Copyright 2011, 2015 Mikhail Lopatkin
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
import name.mlopatkin.andlogview.ui.indexframe.AbstractIndexController;
import name.mlopatkin.andlogview.ui.indexframe.IndexController;
import name.mlopatkin.andlogview.ui.indexframe.IndexFrame;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableModel;
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameDependencies;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;
import name.mlopatkin.andlogview.widgets.UiHelper;

import java.awt.event.KeyEvent;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JTable;

@MainFrameScoped
public class BookmarkController extends AbstractIndexController implements IndexController {
    private final JTable mainLogTable;

    private final IndexFrame indexFrame;

    @Inject
    public BookmarkController(
            BookmarkModel bookmarksModel,
            LogRecordTableModel logModel,
            BookmarksLogModelFilter logModelFilter,
            @Named(MainFrameDependencies.FOR_MAIN_FRAME) JTable mainLogTable,
            DialogFactory dialogFactory) {
        super(mainLogTable);
        this.mainLogTable = mainLogTable;

        BookmarkModel.Observer bookmarkChangeObserver = new BookmarkModel.Observer() {
            @Override
            public void onBookmarkAdded() {
                if (!indexFrame.isVisible()) {
                    showWindow();
                }
                redrawMainTable();
            }

            @Override
            public void onBookmarkRemoved() {
                redrawMainTable();
            }
        };
        bookmarksModel.asObservable().addObserver(bookmarkChangeObserver);

        var builder = DaggerBookmarksDi_BookmarksFrameComponent.builder()
                .bookmarkModel(bookmarksModel)
                .logRecordTableModel(logModel)
                .dialogFactory(dialogFactory)
                .setIndexController(this)
                .setIndexFilter(logModelFilter);
        var indexFrameComponent = (BookmarksDi.BookmarksFrameComponent) builder.build();

        indexFrame = indexFrameComponent.createFrame();
        indexFrame.setTitle("Bookmarks");

        // TODO(mlopatkin) There should be a better way of wiring together key bindings, menus and actions.
        UiHelper.bindKeyGlobal(indexFrame,
                KeyEvent.VK_DELETE,
                "deleteBookmark",
                indexFrameComponent.createDeleteBookmarksAction());
    }

    private void redrawMainTable() {
        mainLogTable.repaint();
    }

    public void showWindow() {
        indexFrame.setVisible(true);
    }
}

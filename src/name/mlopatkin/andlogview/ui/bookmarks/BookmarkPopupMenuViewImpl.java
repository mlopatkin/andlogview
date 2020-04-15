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

import name.mlopatkin.andlogview.ui.logtable.PopupMenuViewImpl;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.widgets.ObservableAction;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class BookmarkPopupMenuViewImpl extends PopupMenuViewImpl
        implements BookmarkPopupMenuPresenter.BookmarkPopupMenuView {

    private final ObservableAction acDeleteBookmarks = new ObservableAction("Remove from bookmarks") {
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));
        }
    };

    public BookmarkPopupMenuViewImpl(JComponent owner, int x, int y) {
        super(owner, x, y);
    }

    @Override
    public Observable<Runnable> setDeleteBookmarksActionEnabled(boolean enabled) {
        if (popupMenu.getComponentCount() > 0) {
            popupMenu.addSeparator();
        }
        acDeleteBookmarks.setEnabled(enabled);
        popupMenu.add(acDeleteBookmarks);
        return acDeleteBookmarks.asObservable();
    }
}

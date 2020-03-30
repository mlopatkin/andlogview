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

package org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.popupmenu;

import com.google.common.collect.ImmutableList;

import org.bitbucket.mlopatkin.utils.events.Observable;
import org.bitbucket.mlopatkin.utils.events.Subject;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class FakeTablePopupMenuView implements TablePopupMenuPresenter.TablePopupMenuView {
    public enum MenuElements {
        HEADER,
        COPY_ACTION,
        BOOKMARK_ACTION,
        QUICK_FILTER_ACTION;
    }

    private @Nullable String headerColumn;

    private @Nullable String headerText;

    private boolean bookmarkActionEnabled;
    private final Subject<Runnable> bookmarkAction = new Subject<>();

    private boolean copyActionEnabled;

    private boolean isShowing;

    private final List<Subject<Runnable>> quickFilterActions = new ArrayList<>();

    private final List<MenuElements> menuElements = new ArrayList<>();

    @Override
    public void setHeader(String columnName, String headerText) {
        addMenuElement(MenuElements.HEADER);
        this.headerColumn = columnName;
        this.headerText = headerText;
    }

    @Override
    public Observable<Runnable> setBookmarkAction(boolean enabled, String title) {
        addMenuElement(MenuElements.BOOKMARK_ACTION);
        bookmarkActionEnabled = enabled;
        return bookmarkAction.asObservable();
    }

    @Override
    public Observable<Runnable> addQuickFilterAction(boolean enabled, String title) {
        // Note that addMenuElement isn't used deliberately because adding multiple actions isn't a error.
        menuElements.add(MenuElements.QUICK_FILTER_ACTION);
        Subject<Runnable> action = new Subject<>();
        quickFilterActions.add(action);
        return action.asObservable();
    }

    @Override
    public void setCopyActionEnabled(boolean enabled) {
        addMenuElement(MenuElements.COPY_ACTION);
        copyActionEnabled = enabled;
    }

    @Override
    public void show() {
        isShowing = true;
    }

    public boolean isHeaderShowing() {
        return headerColumn != null && headerText != null;
    }

    public boolean isCopyActionEnabled() {
        Assert.assertTrue("Copy action isn't added", menuElements.contains(MenuElements.COPY_ACTION));
        return copyActionEnabled;
    }

    public boolean isBookmarkActionEnabled() {
        return bookmarkActionEnabled;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void triggerBookmarkAction() {
        Assert.assertTrue("Bookmark action is not set up", menuElements.contains(MenuElements.BOOKMARK_ACTION));
        Assert.assertTrue("Bookmark action isn't enabled", bookmarkActionEnabled);
        Assert.assertTrue("Popup menu isn't shown", isShowing);

        for (Runnable runnable : bookmarkAction) {
            runnable.run();
        }
    }

    private void addMenuElement(MenuElements header) {
        Assert.assertFalse("Menu element already added", menuElements.contains(header));
        menuElements.add(header);
    }

    public ImmutableList<MenuElements> getMenuElements() {
        return ImmutableList.copyOf(menuElements);
    }

    public void triggerQuickFilterAction(int index) {
        for (Runnable runnable : quickFilterActions.get(index)) {
            runnable.run();
        }
    }

    public int getQuickFilterElementsCount() {
        return quickFilterActions.size();
    }

    public @Nullable String getHeaderColumn() {
        return headerColumn;
    }

    public @Nullable String getHeaderText() {
        return headerText;
    }
}

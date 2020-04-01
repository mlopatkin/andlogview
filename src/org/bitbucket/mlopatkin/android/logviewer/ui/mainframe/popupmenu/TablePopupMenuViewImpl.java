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

import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.PopupMenuViewImpl;
import org.bitbucket.mlopatkin.android.logviewer.widgets.ObservableAction;
import org.bitbucket.mlopatkin.utils.MyStringUtils;
import org.bitbucket.mlopatkin.utils.events.Observable;
import org.bitbucket.mlopatkin.utils.events.Subject;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;

public class TablePopupMenuViewImpl extends PopupMenuViewImpl implements TablePopupMenuPresenter.TablePopupMenuView {
    private static final int MAX_HEADER_LENGTH = 30;
    private static final int PREFIX_LENGTH = 5;
    private static final char ELLIPSIS = '\u2026';  // …

    private final ObservableAction bookmarkAction = new ObservableAction();
    private final JMenu quickFiltersGroup = new JMenu();
    private final ObservableAction quickFilterDialogAction = new ObservableAction();
    private final JMenu highlightFiltersGroup = new JMenu();

    public TablePopupMenuViewImpl(JComponent owner, int x, int y) {
        super(owner, x, y);
    }

    @Override
    public void setHeader(String columnName, String headerText) {
        String header = columnName + ": " + MyStringUtils.abbreviateMiddle(headerText, ELLIPSIS, MAX_HEADER_LENGTH,
                PREFIX_LENGTH);
        quickFiltersGroup.setText(header);
        popupMenu.add(quickFiltersGroup).setEnabled(false);
    }

    @Override
    public Observable<Runnable> setBookmarkAction(boolean enabled, String title) {
        bookmarkAction.setEnabled(enabled);
        bookmarkAction.putValue(Action.NAME, title);
        if (popupMenu.getComponentCount() > 0) {
            popupMenu.addSeparator();
        }
        popupMenu.add(bookmarkAction);
        return bookmarkAction.asObservable();
    }

    @Override
    public Observable<Runnable> addQuickFilterDialogAction(String title) {
        quickFilterDialogAction.putValue(Action.NAME, title);
        quickFiltersGroup.add(quickFilterDialogAction);
        return quickFilterDialogAction.asObservable();
    }

    @Override
    public Observable<Runnable> addQuickFilterAction(String title) {
        ObservableAction quickFilterAction = new ObservableAction(title);
        quickFiltersGroup.setEnabled(true);
        quickFiltersGroup.add(quickFilterAction);
        return quickFilterAction.asObservable();
    }

    @Override
    public Observable<Consumer<Color>> addHighlightFilterAction(String title, List<Color> highlightColors) {
        quickFiltersGroup.setEnabled(true);
        quickFiltersGroup.add(highlightFiltersGroup);
        highlightFiltersGroup.setText(title);

        Subject<Consumer<Color>> highlightSubject = new Subject<>();

        int colorIndex = 0;
        for (Color color : highlightColors) {
            Action highlightAction = new AbstractAction("Color " + (colorIndex++), new HighlightIcon(color)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (Consumer<Color> colorConsumer : highlightSubject) {
                        colorConsumer.accept(color);
                    }
                }
            };
            highlightFiltersGroup.add(highlightAction);
        }

        return highlightSubject.asObservable();
    }
}

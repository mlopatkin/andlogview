/*
 * Copyright 2021 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.ui.status;

import name.mlopatkin.andlogview.utils.CommonChars;
import name.mlopatkin.andlogview.utils.MyStringUtils;
import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;
import name.mlopatkin.andlogview.widgets.ObservableAction;
import name.mlopatkin.andlogview.widgets.UiHelper;

import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

class SourceStatusViewImpl implements SourceStatusPresenter.View {
    private final JLabel sourceStatusLabel;
    private final Subject<Consumer<SourceStatusPopupMenuView>> popupMenuAction = new Subject<>();

    public SourceStatusViewImpl(JLabel sourceStatusLabel) {
        this.sourceStatusLabel = sourceStatusLabel;

        UiHelper.addPopupMenu(sourceStatusLabel, this::showMenu);
    }

    @Override
    public void showWaitingStatus() {
        setLabelText("Waiting for a device" + CommonChars.ELLIPSIS);
    }

    @Override
    public void showSourceStatus(String status) {
        setLabelText(status);
    }

    private void setLabelText(String text) {
        sourceStatusLabel.setText(text);
    }

    @Override
    public Observable<Consumer<SourceStatusPopupMenuView>> popupMenuAction() {
        return popupMenuAction.asObservable();
    }

    private void showMenu(JLabel invoker, int x, int y) {
        PopupMenu popupMenu = new PopupMenu(invoker, x, y);
        for (Consumer<SourceStatusPopupMenuView> consumer : popupMenuAction) {
            consumer.accept(popupMenu);
        }
    }

    private static class PopupMenu implements SourceStatusPopupMenuView {
        private final JPopupMenu popupMenu = new JPopupMenu();
        private final JComponent invoker;
        private final int x;
        private final int y;

        PopupMenu(JComponent invoker, int x, int y) {
            this.invoker = invoker;
            this.x = x;
            this.y = y;
        }

        @Override
        public Observable<Runnable> addCopyAction(String itemName, String value) {
            ObservableAction action = new ObservableAction(formatCopyActionTitle(itemName, value));
            popupMenu.add(action);
            return action.asObservable();
        }

        @Override
        public void show() {
            popupMenu.show(invoker, x, y);
        }

        private String formatCopyActionTitle(String itemName, String value) {
            String abbreviatedValue = MyStringUtils.abbreviateMiddle(value, CommonChars.ELLIPSIS, 80, 20);
            return String.format("Copy %s (%s)", itemName, abbreviatedValue);
        }
    }
}

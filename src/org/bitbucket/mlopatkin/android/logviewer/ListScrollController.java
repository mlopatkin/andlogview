/*
 * Copyright 2011 Mikhail Lopatkin
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
package org.bitbucket.mlopatkin.android.logviewer;

import javax.swing.JList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Autoscrolling controller for a {@link JList}.
 */
public class ListScrollController extends AutoScrollController {

    private JList list;

    private final ListDataListener modelListener = new ListDataListener() {

        @Override
        public void contentsChanged(ListDataEvent e) {
            scrollIfNeeded();
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            scrollIfNeeded();
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            scrollIfNeeded();
        }
    };

    public ListScrollController(JList list) {
        super(list);
        this.list = list;
        list.getModel().addListDataListener(modelListener);
    }

    @Override
    protected void performScrollToTheEnd() {
        int lastIndex = list.getModel().getSize() - 1;
        list.scrollRectToVisible(list.getCellBounds(lastIndex, lastIndex));
    }

}

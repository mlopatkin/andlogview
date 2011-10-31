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

import java.awt.Container;
import java.awt.EventQueue;

import javax.swing.JComponent;

/**
 * This class is responsible for scrolling a table when new records are added
 * into it or table's size has been changed. The scrolling to the last record is
 * performed only if the table was already scrolled to the bottom before the
 * insertion.
 * <p>
 * The usage pattern is following: whenever you want to insert a record or
 * change size of the table call {@link #notifyBeforeInsert()} before your
 * action. If you aren't inserting something you should call
 * {@link #scrollIfNeeded()} after your action.
 */
abstract class AutoScrollController {

    private boolean shouldScroll;
    private JComponent scrollable;

    public AutoScrollController(JComponent scrollable) {
        this.scrollable = scrollable;
    }

    private boolean isAtBottom() {
        Container parent = scrollable.getParent();
        int bottom = scrollable.getBounds().height;
        int pHeight = parent.getBounds().height;
        int y = scrollable.getBounds().y;
        boolean atBottom = (pHeight - y) == bottom;
        return atBottom;
    }

    /**
     * Call this before inserting anything into the table or changing its size.
     * <p>
     * This methods checks if scrolling is needed after the action.
     * <p>
     * NOTE: this should be called on the UI thread.
     */
    public void notifyBeforeInsert() {
        assert EventQueue.isDispatchThread();
        shouldScroll = isAtBottom();
    }

    private final Runnable scrollToTheEnd = new Runnable() {
        @Override
        public void run() {
            performScrollToTheEnd();
        }
    };

    /**
     * Call this method after any action other then inserting something into the
     * table.
     * <p>
     * NOTE: this should be called on the UI thread.
     */
    public void scrollIfNeeded() {
        assert EventQueue.isDispatchThread();
        if (shouldScroll) {
            EventQueue.invokeLater(scrollToTheEnd);
        }
    }

    protected abstract void performScrollToTheEnd();

    protected final void resetScroll() {
        shouldScroll = false;
    }
}

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

public class BookmarksPopupMenuHandler extends TablePopupMenuHandler {

    private JMenuItem unmarkThis = new JMenuItem("Remove from bookmarks");
    private BookmarksController controller;

    private void setUpMenu() {
        JPopupMenu menu = getMenu();
        unmarkThis.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.unmarkRecord(getRow());
            }
        });
        menu.add(unmarkThis);
    }

    public BookmarksPopupMenuHandler(JTable table, BookmarksController controller) {
        super(table);
        setUpMenu();
        this.controller = controller;
    }

}

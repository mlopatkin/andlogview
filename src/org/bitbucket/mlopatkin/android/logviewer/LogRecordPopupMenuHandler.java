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

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.filters.MultiPidFilter;
import org.bitbucket.mlopatkin.android.liblogcat.filters.SingleTagFilter;

public class LogRecordPopupMenuHandler extends TablePopupMenuHandler {

    private JMenuItem hideWithThisTag = new JMenuItem("Hide with this tag");
    private JMenuItem hideWithThisPid = new JMenuItem("Hide with this pid");
    private JMenuItem pinThisLine = new JMenuItem("Pin this line");
    private JMenuItem resetLog = new JMenuItem("Reset logs");

    private PinRecordsController pinRecordsController;
    private FilterController filterController;
    private MainFrame main;

    private void setUpMenu() {
        hideWithThisTag.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                filterController.addFilter(FilteringMode.HIDE, new SingleTagFilter(
                        getLogRecordAtPoint().getTag()));
            }
        });

        hideWithThisPid.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                filterController.addFilter(FilteringMode.HIDE, new MultiPidFilter(
                        new int[] { getLogRecordAtPoint().getPid() }));
            }
        });

        pinThisLine.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                pinRecordsController.pinRecord(getRow());
            }
        });

        resetLog.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                main.reset();
            }
        });

        JPopupMenu popupMenu = getMenu();
        popupMenu.add(hideWithThisTag);
        popupMenu.add(hideWithThisPid);
        popupMenu.add(pinThisLine);
        popupMenu.addSeparator();
        popupMenu.add(resetLog);
    }

    public LogRecordPopupMenuHandler(MainFrame main, JTable table,
            final FilterController filterController, final PinRecordsController pinRecordsController) {
        super(table);
        this.main = main;
        this.filterController = filterController;
        this.pinRecordsController = pinRecordsController;
        setUpMenu();
    }

    private LogRecord getLogRecordAtPoint() {
        return ((LogRecordTableModel) getTable().getModel()).getRowData(getRow());
    }

}

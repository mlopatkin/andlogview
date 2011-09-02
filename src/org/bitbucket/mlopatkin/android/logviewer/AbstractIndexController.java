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

import javax.swing.JTable;

public abstract class AbstractIndexController implements IndexController {

    private JTable mainTable;
    private LogRecordTableModel model;

    private IndexFrame indexFrame;

    public AbstractIndexController(JTable mainTable, LogRecordTableModel model,
            PidToProcessMapper mapper, FilterController filterController) {
        this.mainTable = mainTable;
        this.model = model;

        indexFrame = new IndexFrame(this.model, new IndexTableColumnModel(mapper), this);

        filterController.addRefreshListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                onMainTableUpdate();
            }
        });
    }

    @Override
    public void activateRow(int row) {
        int rowTable = mainTable.convertRowIndexToView(row);
        mainTable.getSelectionModel().setSelectionInterval(rowTable, rowTable);
        mainTable.scrollRectToVisible(mainTable.getCellRect(rowTable,
                mainTable.getSelectedColumn(), false));
    }

    protected abstract void onMainTableUpdate();

    public IndexFrame getFrame() {
        return indexFrame;
    }

    public void showWindow() {
        indexFrame.setVisible(true);
    }

    public void hideWindow() {
        indexFrame.setVisible(false);
    }

    @Override
    public void onWindowClosed() {
        // do nothing
    }
}

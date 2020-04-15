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
package name.mlopatkin.andlogview.ui.indexframe;

import javax.swing.JTable;

public abstract class AbstractIndexController implements IndexController {
    private JTable mainTable;

    public AbstractIndexController(JTable mainTable) {
        this.mainTable = mainTable;
    }

    @Override
    public void activateRow(int row) {
        int rowTable = mainTable.convertRowIndexToView(row);
        mainTable.getSelectionModel().setSelectionInterval(rowTable, rowTable);
        mainTable.scrollRectToVisible(mainTable.getCellRect(rowTable, mainTable.getSelectedColumn(), false));
    }

    @Override
    public void onWindowClosed() {
        // do nothing
    }
}

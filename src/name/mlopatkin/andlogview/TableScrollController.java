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
package name.mlopatkin.andlogview;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

class TableScrollController extends AutoScrollController {
    private final JTable table;

    public TableScrollController(JTable table) {
        super(table);
        this.table = table;
        TableModel model = table.getModel();
        TableModelListener modelListener = e -> {
            if (e.getType() == TableModelEvent.INSERT) {
                scrollIfNeeded();
            } else {
                resetScroll();
            }
        };
        model.addTableModelListener(modelListener);
    }

    @Override
    protected void performScrollToTheEnd() {
        table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, TableModelEvent.ALL_COLUMNS, true));
    }
}

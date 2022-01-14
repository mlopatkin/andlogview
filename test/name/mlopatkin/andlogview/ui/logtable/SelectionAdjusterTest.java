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

package name.mlopatkin.andlogview.ui.logtable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import name.mlopatkin.andlogview.test.TestData;

import org.junit.Test;

public class SelectionAdjusterTest {

    @Test
    public void showContextMenuClearsSelectionIfClickedOutsideOfRows() {
        SelectedRows selectedRows = new TestSelectedRows(makeRow(1));

        SelectionAdjuster adjuster = new SelectionAdjuster(selectedRows);

        adjuster.onPopupMenuShown(null);

        assertThat(selectedRows.getSelectedRows(), is(empty()));
    }

    @Test
    public void showContextMenuAddsSelectionIfNothingSelected() {
        SelectedRows selectedRows = new TestSelectedRows();

        SelectionAdjuster adjuster = new SelectionAdjuster(selectedRows);

        adjuster.onPopupMenuShown(makeRow(1));

        assertThat(selectedRows.getSelectedRows(), contains(makeRow(1)));
    }

    @Test
    public void showContextMenuReplacesSelectionIfRowNotSelected() {
        SelectedRows selectedRows = new TestSelectedRows(makeRow(1));

        SelectionAdjuster adjuster = new SelectionAdjuster(selectedRows);

        adjuster.onPopupMenuShown(makeRow(2));

        assertThat(selectedRows.getSelectedRows(), contains(makeRow(2)));
    }

    @Test
    public void showContextMenuKeepsSelectionIfClickedRowAlreadySelected() {
        SelectedRows selectedRows = new TestSelectedRows(makeRow(1), makeRow(2));

        SelectionAdjuster adjuster = new SelectionAdjuster(selectedRows);

        adjuster.onPopupMenuShown(makeRow(1));

        assertThat(selectedRows.getSelectedRows(), contains(makeRow(1), makeRow(2)));
    }

    private static TableRow makeRow(int index) {
        return new TableRow(index, TestData.RECORD1);
    }
}

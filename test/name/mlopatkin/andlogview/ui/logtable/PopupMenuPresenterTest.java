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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import name.mlopatkin.andlogview.test.TestData;
import name.mlopatkin.andlogview.ui.logtable.PopupMenuPresenter.PopupMenuView;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

public class PopupMenuPresenterTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);


    @Mock
    PopupMenuPresenter.PopupMenuView popupMenuView;

    @Test
    public void contextMenuIsShownIfSelectionEmpty() {
        PopupMenuPresenter<PopupMenuView> presenter = createPresenter();

        presenter.showContextMenu(popupMenuView, Column.PID, null);
        verify(popupMenuView).show();
    }

    @Test
    public void contextMenuIsShownIfSelectionSingle() {
        PopupMenuPresenter<PopupMenuView> presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        verify(popupMenuView).show();
    }

    @Test
    public void contextMenuIsShownIfSelectionMultiple() {
        PopupMenuPresenter<PopupMenuView> presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(2));

        verify(popupMenuView).show();
    }

    @Test
    public void contextMenuHasCopyDisableIfSelectionEmpty() {
        PopupMenuPresenter<PopupMenuView> presenter = createPresenter();
        presenter.showContextMenu(popupMenuView, Column.PID, null);

        verify(popupMenuView).setCopyActionEnabled(false);
        verify(popupMenuView, never()).setCopyActionEnabled(true);
    }

    @Test
    public void contextMenuHasCopyEnabledIfSingleRowSelected() {
        PopupMenuPresenter<PopupMenuView> presenter = createPresenter(makeRow(1));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(1));

        verify(popupMenuView).setCopyActionEnabled(true);
        verify(popupMenuView, never()).setCopyActionEnabled(false);
    }

    @Test
    public void contextMenuHasCopyEnabledIfMultipleRowsSelected() {
        PopupMenuPresenter<PopupMenuView> presenter = createPresenter(makeRow(1), makeRow(2));
        presenter.showContextMenu(popupMenuView, Column.PID, makeRow(2));

        verify(popupMenuView).setCopyActionEnabled(true);
        verify(popupMenuView, never()).setCopyActionEnabled(false);
    }

    private PopupMenuPresenter<PopupMenuView> createPresenter(TableRow... rows) {
        SelectedRows selectedRows = new TestSelectedRows(rows);
        return new PopupMenuPresenter<>(selectedRows);
    }

    private static TableRow makeRow(int index) {
        return new TableRow(index, TestData.RECORD1);
    }
}

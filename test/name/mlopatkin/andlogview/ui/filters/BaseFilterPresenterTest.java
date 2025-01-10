/*
 * Copyright 2024 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.filters;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.filters.FilteringMode;
import name.mlopatkin.andlogview.filters.MutableFilterModel;
import name.mlopatkin.andlogview.search.RequestCompilationException;
import name.mlopatkin.andlogview.ui.filterdialog.FilterDialogFactory;
import name.mlopatkin.andlogview.ui.filterdialog.FilterDialogHandle;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialogData;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
class BaseFilterPresenterTest {
    @Mock
    FilterDialogFactory dialogFactory;

    private final MutableFilterModel filterModel = MutableFilterModel.create();

    @Test
    void openingEditDialogForTheSecondTimeBringsItToFront() {
        var filter = createFilter();
        var mockHandle = mockHandle();
        when(dialogFactory.startEditFilterDialog(filter)).thenReturn(mockHandle);

        var pf = createPanelFilter(filter);
        pf.openFilterEditor();
        pf.openFilterEditor();

        var inOrder = inOrder(mockHandle);
        inOrder.verify(mockHandle).getResult();
        inOrder.verify(mockHandle).bringToFront();
    }

    BaseFilterPresenter createPanelFilter(FilterFromDialog filter) {
        return new BaseFilterPresenter(filterModel, dialogFactory, filter) {};
    }

    FilterFromDialog createFilter() {
        try {
            return new FilterFromDialogData(FilteringMode.SHOW)
                    .setMessagePattern("message")
                    .toFilter();
        } catch (RequestCompilationException e) {
            throw new AssertionError(e);
        }
    }

    FilterDialogHandle mockHandle() {
        var future = new CompletableFuture<Optional<FilterFromDialog>>();
        FilterDialogHandle handle = mock();
        lenient().when(handle.getResult()).thenReturn(future);
        return handle;
    }
}

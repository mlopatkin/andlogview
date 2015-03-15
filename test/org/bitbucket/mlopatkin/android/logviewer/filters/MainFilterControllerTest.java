/*
 * Copyright 2015 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.filters;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.CreateFilterDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.EditFilterDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterDialogFactory;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterFromDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.FilterPanelModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.PanelFilter;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexfilter.IndexFilterCollection;
import org.bitbucket.mlopatkin.utils.events.Observable;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.Color;

import javax.annotation.Nullable;

import static org.bitbucket.mlopatkin.android.logviewer.test.TestData.MATCH_ALL;
import static org.bitbucket.mlopatkin.android.logviewer.test.TestData.MATCH_FIRST;
import static org.bitbucket.mlopatkin.android.logviewer.test.TestData.RECORD1;
import static org.bitbucket.mlopatkin.android.logviewer.test.TestData.RECORD1_IN_MAIN;
import static org.bitbucket.mlopatkin.android.logviewer.test.TestData.RECORD2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MainFilterControllerTest {

    @Mock
    FilterPanelModel filterPanelModel;
    @Mock
    IndexFilterCollection indexFilterCollection;
    @Mock
    Observable<IndexFilterCollection.Observer> indexFilterCollectionObservers;
    @Mock
    FilterDialogFactory dialogFactory;

    @Captor
    ArgumentCaptor<CreateFilterDialog.DialogResultReceiver> createResultReceiver;

    @Captor
    ArgumentCaptor<EditFilterDialog.DialogResultReceiver> editResultReceiver;

    @Captor
    ArgumentCaptor<PanelFilter> panelFilterCaptor;

    @Captor
    ArgumentCaptor<FilterFromDialog> oldFilterCaptor;

    InOrder order;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(indexFilterCollection.asObservable()).thenReturn(indexFilterCollectionObservers);
    }


    @Test
    public void testInitialState() throws Exception {
        MainFilterController controller =
                new MainFilterController(filterPanelModel, indexFilterCollection, dialogFactory);

        verify(indexFilterCollectionObservers).addObserver(any(IndexFilterCollection.Observer.class));

        assertTrue(controller.shouldShowRecord(RECORD1));
        // default state for buffers is to be all disabled but unknown because we cannot read it
        assertFalse(controller.shouldShowRecord(RECORD1_IN_MAIN));
        assertTrue(controller.shouldShowRecord(RECORD2));
        assertNull(controller.getHighlightColor(RECORD1));
        assertNull(controller.getHighlightColor(RECORD1_IN_MAIN));
        assertNull(controller.getHighlightColor(RECORD2));
    }

    @Test
    @Ignore("Known to be broken")
    public void testTrickyColorFilterReplacement() throws Exception {
        // The trick here is that for highlight filter the order of filters matters.
        // We are trying to color by the rightmost filter (added last).
        // Now filter editing preserves its place in the model.
        // However if you change filter's mode from highlight to other and back - it becomes last in the highlight
        // chain. Its button however stays in the same place.
        MainFilterController controller =
                new MainFilterController(filterPanelModel, indexFilterCollection, dialogFactory);

        order = inOrder(dialogFactory, filterPanelModel);
        FilterFromDialog colorer1 = createColoringFilter(Color.BLACK, MATCH_ALL);
        FilterFromDialog colorer2 = createColoringFilter(Color.BLUE, MATCH_ALL);

        FilterFromDialog editedColorer1 = createMockFilter(FilteringMode.SHOW, MATCH_FIRST);

        PanelFilter colorer1Panel = createFilterWithDialog(controller, colorer1);
        createFilterWithDialog(controller, colorer2);

        PanelFilter editedPanel = editFilterWithDialog(editedColorer1, colorer1Panel);
        editFilterWithDialog(colorer1, editedPanel);

        assertTrue(controller.shouldShowRecord(RECORD2));
        assertEquals(Color.BLUE, controller.getHighlightColor(RECORD2));
    }

    private PanelFilter createFilterWithDialog(MainFilterController controller, FilterFromDialog dialogResult) {
        controller.createFilterWithDialog();
        order.verify(dialogFactory).startCreateFilterDialog(createResultReceiver.capture());
        CreateFilterDialog dialog = mock(CreateFilterDialog.class);
        when(dialog.createFilter()).thenReturn(dialogResult);
        createResultReceiver.getValue().onDialogResult(dialog, true);

        order.verify(filterPanelModel).addFilter(panelFilterCaptor.capture());
        return panelFilterCaptor.getValue();
    }

    private PanelFilter editFilterWithDialog(FilterFromDialog newFilter, PanelFilter oldFilter) {
        oldFilter.openFilterEditor();
        order.verify(dialogFactory).startEditFilterDialog(oldFilterCaptor.capture(), editResultReceiver.capture());
        EditFilterDialog dialog = mock(EditFilterDialog.class);
        when(dialog.createFilter()).thenReturn(newFilter);
        editResultReceiver.getValue().onDialogResult(oldFilterCaptor.getValue(), Optional.of(newFilter), true);

        order.verify(filterPanelModel).replaceFilter(eq(oldFilter), panelFilterCaptor.capture());
        return panelFilterCaptor.getValue();
    }

    private FilterFromDialog createMockFilter(FilteringMode mode, final Predicate<LogRecord> predicate) {
        FilterFromDialog result = new FilterFromDialog() {
            @Override
            public boolean apply(@Nullable LogRecord input) {
                return predicate.apply(input);
            }
        };
        result.setMode(mode);
        return result;
    }

    private FilterFromDialog createColoringFilter(Color color, final Predicate<LogRecord> predicate) {
        FilterFromDialog result = createMockFilter(FilteringMode.HIGHLIGHT, predicate);
        result.setHighlightColor(color);
        return result;
    }
}

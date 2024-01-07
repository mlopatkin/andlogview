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

package name.mlopatkin.andlogview.filters;

import static name.mlopatkin.andlogview.test.TestData.MATCH_ALL;
import static name.mlopatkin.andlogview.test.TestData.MATCH_FIRST;
import static name.mlopatkin.andlogview.test.TestData.RECORD1;
import static name.mlopatkin.andlogview.test.TestData.RECORD1_IN_MAIN;
import static name.mlopatkin.andlogview.test.TestData.RECORD2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.ui.filterdialog.FilterDialogFactory;
import name.mlopatkin.andlogview.ui.filterdialog.FilterDialogHandle;
import name.mlopatkin.andlogview.ui.filterdialog.FilterFromDialog;
import name.mlopatkin.andlogview.ui.filterpanel.FilterPanelModel;
import name.mlopatkin.andlogview.ui.filterpanel.PanelFilter;
import name.mlopatkin.andlogview.ui.indexfilter.IndexFilterCollection;
import name.mlopatkin.andlogview.utils.events.Observable;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import java.awt.Color;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class MainFilterControllerTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    FilterPanelModel filterPanelModel;
    @Mock
    IndexFilterCollection indexFilterCollection;
    @Mock
    Observable<IndexFilterCollection.Observer> indexFilterCollectionObservers;
    @Mock
    FilterDialogFactory dialogFactory;

    @Captor
    ArgumentCaptor<PanelFilter> panelFilterCaptor;

    final FilterModel filterModel = new FilterModelImpl();
    final LogModelFilterImpl filterImpl = new LogModelFilterImpl(filterModel);

    @MonotonicNonNull InOrder order;

    @Before
    public void setUp() throws Exception {
        when(indexFilterCollection.asObservable()).thenReturn(indexFilterCollectionObservers);
    }

    @Test
    public void testInitialState() throws Exception {
        new MainFilterController(filterPanelModel, indexFilterCollection, dialogFactory, filterModel);

        verify(indexFilterCollectionObservers).addObserver(any(IndexFilterCollection.Observer.class));

        assertTrue(filterImpl.shouldShowRecord(RECORD1));
        // default state for buffers is to be all disabled but unknown because we cannot read it
        assertFalse(filterImpl.shouldShowRecord(RECORD1_IN_MAIN));
        assertTrue(filterImpl.shouldShowRecord(RECORD2));
        assertNull(filterImpl.getHighlightColor(RECORD1));
        assertNull(filterImpl.getHighlightColor(RECORD1_IN_MAIN));
        assertNull(filterImpl.getHighlightColor(RECORD2));
    }

    @Test
    @Ignore("Known to be broken")
    public void testTrickyColorFilterReplacement() throws Exception {
        // The trick here is that for highlight filter the order of filters matters.
        // We are trying to color by the rightmost filter (added last).
        // Now filter editing preserves its place in the model.
        // However, if you change filter's mode from highlight to other and back - it becomes last in the highlight
        // chain. Its button however stays in the same place.
        MainFilterController controller = new MainFilterController(
                filterPanelModel, indexFilterCollection, dialogFactory, filterModel);

        order = inOrder(dialogFactory, filterPanelModel);
        FilterFromDialog colorer1 = createColoringFilter(Color.BLACK, MATCH_ALL);
        FilterFromDialog colorer2 = createColoringFilter(Color.BLUE, MATCH_ALL);

        FilterFromDialog editedColorer1 = createMockFilter(FilteringMode.SHOW, MATCH_FIRST);

        PanelFilter colorer1Panel = createFilterWithDialog(controller, colorer1);
        createFilterWithDialog(controller, colorer2);

        PanelFilter editedPanel = editFilterWithDialog(editedColorer1, colorer1Panel);
        editFilterWithDialog(colorer1, editedPanel);

        assertTrue(filterImpl.shouldShowRecord(RECORD2));
        assertEquals(Color.BLUE, filterImpl.getHighlightColor(RECORD2));
    }

    @Test
    public void testSavingAndInitializngFromSaved() throws Exception {
        MainFilterController controller = new MainFilterController(
                filterPanelModel, indexFilterCollection, dialogFactory, filterModel);

        order = inOrder(dialogFactory, filterPanelModel);

        FilterFromDialog colorer = createColoringFilter(Color.BLACK, MATCH_ALL);
        createFilterWithDialog(controller, colorer);

        filterPanelModel = mock(FilterPanelModel.class);
        new MainFilterController(filterPanelModel, indexFilterCollection, dialogFactory, filterModel);

        verify(filterPanelModel).addFilter(Mockito.any());
        assertEquals(Color.BLACK, filterImpl.getHighlightColor(RECORD2));
    }

    @Test
    public void editFilterTwiceDoesntCrash() {
        MainFilterController controller = new MainFilterController(
                filterPanelModel, indexFilterCollection, dialogFactory, filterModel);

        order = inOrder(dialogFactory, filterPanelModel);
        FilterFromDialog initialFilter = createColoringFilter(Color.BLACK, MATCH_ALL);

        PanelFilter initialPanel = createFilterWithDialog(controller, initialFilter);

        CompletableFuture<Optional<FilterFromDialog>> firstDialog = openFilterDialog(initialPanel);
        CompletableFuture<Optional<FilterFromDialog>> secondDialog = openFilterDialog(initialPanel);

        firstDialog.complete(Optional.of(createColoringFilter(Color.BLUE, MATCH_ALL)));
        secondDialog.complete(Optional.of(createColoringFilter(Color.RED, MATCH_ALL)));

        assertEquals(Color.BLUE, filterImpl.getHighlightColor(RECORD1));
    }

    @Test
    public void editDisabledFilterKeepsItDisabled() {
        MainFilterController controller = new MainFilterController(
                filterPanelModel, indexFilterCollection, dialogFactory, filterModel);
        order = inOrder(dialogFactory, filterPanelModel);
        FilterFromDialog initialFilter = createColoringFilter(Color.BLACK, MATCH_ALL);
        PanelFilter initialPanel = createFilterWithDialog(controller, initialFilter);
        PanelFilter disabledPanel = toggle(initialPanel);

        CompletableFuture<Optional<FilterFromDialog>> editor = openFilterDialog(disabledPanel);
        editor.complete(Optional.of(createColoringFilter(Color.BLUE, MATCH_ALL)));

        assertNull(filterImpl.getHighlightColor(RECORD1));
    }

    private PanelFilter toggle(PanelFilter toggledFilter) {
        boolean enabled = !toggledFilter.isEnabled();
        toggledFilter.setEnabled(enabled);

        order.verify(filterPanelModel).replaceFilter(eq(toggledFilter), panelFilterCaptor.capture());
        return panelFilterCaptor.getValue();
    }

    private PanelFilter createFilterWithDialog(MainFilterController controller, FilterFromDialog dialogResult) {
        when(dialogFactory.startCreateFilterDialog()).thenReturn(
                CompletableFuture.completedFuture(Optional.of(dialogResult)));
        controller.createFilterWithDialog();

        order.verify(filterPanelModel).addFilter(panelFilterCaptor.capture());
        return panelFilterCaptor.getValue();
    }

    private PanelFilter editFilterWithDialog(FilterFromDialog newFilter, PanelFilter oldFilter) {
        FilterDialogHandle dialogHandle = mock(FilterDialogHandle.class);
        when(dialogHandle.getResult()).thenReturn(CompletableFuture.completedFuture(Optional.of(newFilter)));
        when(dialogFactory.startEditFilterDialog(any())).thenReturn(dialogHandle);
        oldFilter.openFilterEditor();

        order.verify(filterPanelModel).replaceFilter(eq(oldFilter), panelFilterCaptor.capture());
        return panelFilterCaptor.getValue();
    }

    private CompletableFuture<Optional<FilterFromDialog>> openFilterDialog(PanelFilter filter) {
        CompletableFuture<Optional<FilterFromDialog>> future = new CompletableFuture<>();
        FilterDialogHandle dialogHandle = mock(FilterDialogHandle.class);
        lenient().when(dialogHandle.getResult()).thenReturn(future);
        lenient().when(dialogFactory.startEditFilterDialog(any())).thenReturn(dialogHandle);
        filter.openFilterEditor();

        return future;
    }

    private FilterFromDialog createMockFilter(FilteringMode mode, final Predicate<LogRecord> predicate) {
        FilterFromDialog result = new FilterFromDialog() {
            @Override
            public boolean test(@Nullable LogRecord input) {
                return predicate.test(input);
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

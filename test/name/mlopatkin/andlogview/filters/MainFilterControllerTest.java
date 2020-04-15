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

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.config.ConfigStorage;
import org.bitbucket.mlopatkin.android.logviewer.config.FakeDefaultConfigStorage;
import org.bitbucket.mlopatkin.android.logviewer.filters.MainFilterController.SavedFilterData;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterDialogFactory;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterFromDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.FilterPanelModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.PanelFilter;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexfilter.IndexFilterCollection;
import org.bitbucket.mlopatkin.utils.events.Observable;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static org.bitbucket.mlopatkin.android.logviewer.test.TestData.MATCH_ALL;
import static org.bitbucket.mlopatkin.android.logviewer.test.TestData.MATCH_FIRST;
import static org.bitbucket.mlopatkin.android.logviewer.test.TestData.RECORD1;
import static org.bitbucket.mlopatkin.android.logviewer.test.TestData.RECORD1_IN_MAIN;
import static org.bitbucket.mlopatkin.android.logviewer.test.TestData.RECORD2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
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
    ArgumentCaptor<PanelFilter> panelFilterCaptor;

    @Captor
    ArgumentCaptor<List<SavedFilterData>> savedFilterDataCaptor;

    LogModelFilterImpl filterImpl = new LogModelFilterImpl();

    @MonotonicNonNull InOrder order;

    @Mock
    ConfigStorage mockStorage;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(indexFilterCollection.asObservable()).thenReturn(indexFilterCollectionObservers);
    }

    @Test
    public void testInitialState() throws Exception {
        mockStorage = new FakeDefaultConfigStorage();
        MainFilterController controller = new MainFilterController(
                filterPanelModel, indexFilterCollection, dialogFactory, mockStorage, filterImpl);

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
        // However if you change filter's mode from highlight to other and back - it becomes last in the highlight
        // chain. Its button however stays in the same place.
        MainFilterController controller = new MainFilterController(
                filterPanelModel, indexFilterCollection, dialogFactory, mockStorage, filterImpl);

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
        when(mockStorage.loadConfig(Mockito.<FilterListSerializer>any())).thenReturn(Collections.emptyList());

        MainFilterController controller = new MainFilterController(
                filterPanelModel, indexFilterCollection, dialogFactory, mockStorage, filterImpl);

        order = inOrder(dialogFactory, filterPanelModel);

        FilterFromDialog colorer = createColoringFilter(Color.BLACK, MATCH_ALL);
        createFilterWithDialog(controller, colorer);

        verify(mockStorage).saveConfig(Mockito.<FilterListSerializer>any(), savedFilterDataCaptor.capture());

        List<SavedFilterData> savedFilterData = savedFilterDataCaptor.getValue();

        mockStorage = Mockito.mock(ConfigStorage.class);
        when(mockStorage.loadConfig(Mockito.<FilterListSerializer>any())).thenReturn(savedFilterData);

        filterPanelModel = Mockito.mock(FilterPanelModel.class);
        controller = new MainFilterController(
                filterPanelModel, indexFilterCollection, dialogFactory, mockStorage, filterImpl);

        verify(filterPanelModel).addFilter(Mockito.any());
        assertEquals(Color.BLACK, filterImpl.getHighlightColor(RECORD2));
    }

    @Test
    public void editFilterTwiceDoesntCrash() {
        MainFilterController controller = new MainFilterController(
                filterPanelModel, indexFilterCollection, dialogFactory, new FakeDefaultConfigStorage(), filterImpl);

        order = inOrder(dialogFactory, filterPanelModel);
        FilterFromDialog initialFilter = createColoringFilter(Color.BLACK, MATCH_ALL);

        PanelFilter initialPanel = createFilterWithDialog(controller, initialFilter);

        CompletableFuture<Optional<FilterFromDialog>> firstDialog = openFilterDialog(initialPanel);
        CompletableFuture<Optional<FilterFromDialog>> secondDialog = openFilterDialog(initialPanel);

        firstDialog.complete(Optional.of(createColoringFilter(Color.BLUE, MATCH_ALL)));
        secondDialog.complete(Optional.of(createColoringFilter(Color.RED, MATCH_ALL)));

        assertEquals(Color.BLUE, filterImpl.getHighlightColor(RECORD1));
    }

    private PanelFilter createFilterWithDialog(MainFilterController controller, FilterFromDialog dialogResult) {
        when(dialogFactory.startCreateFilterDialog()).thenReturn(
                CompletableFuture.completedFuture(Optional.of(dialogResult)));
        controller.createFilterWithDialog();

        order.verify(filterPanelModel).addFilter(panelFilterCaptor.capture());
        return panelFilterCaptor.getValue();
    }

    private PanelFilter editFilterWithDialog(FilterFromDialog newFilter, PanelFilter oldFilter) {
        when(dialogFactory.startEditFilterDialog(any())).thenReturn(
                CompletableFuture.completedFuture(Optional.of(newFilter)));
        oldFilter.openFilterEditor();

        order.verify(filterPanelModel).replaceFilter(eq(oldFilter), panelFilterCaptor.capture());
        return panelFilterCaptor.getValue();
    }

    private CompletableFuture<Optional<FilterFromDialog>> openFilterDialog(PanelFilter filter) {
        CompletableFuture<Optional<FilterFromDialog>> future = new CompletableFuture<>();
        when(dialogFactory.startEditFilterDialog(any())).thenReturn(future);
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

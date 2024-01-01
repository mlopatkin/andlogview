/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import name.mlopatkin.andlogview.base.concurrent.TestSequentialExecutor;
import name.mlopatkin.andlogview.search.ListSearchModel;
import name.mlopatkin.andlogview.search.RequestCompilationException;
import name.mlopatkin.andlogview.search.SearchModel;
import name.mlopatkin.andlogview.ui.status.SearchStatusPresenter;

import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.MoreExecutors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;
import java.util.function.Predicate;

@ExtendWith(MockitoExtension.class)
class SearchPresenterTest {
    final SearchPatternCompiler<Predicate<Integer>> patternCompiler = SearchPresenterTest::compilePredicate;

    @Spy
    FakeSearchableView searchableView;

    @Mock
    SearchStatusPresenter statusPresenter;

    @Mock
    SearchModel.StrategyObserver<Predicate<Integer>> modelObserver;

    @Spy
    FakeSearchPromptView searchPromptView;

    @Test
    void showsPromptViewWhenRequested() {
        var presenter = createPresenter(createModel(0, 1, 2));

        presenter.showSearchPrompt();

        verify(searchPromptView).show();
    }

    @Test
    void showingPromptTwiceFocuses() {
        var presenter = createPresenter(createModel(0, 1, 2));
        var order = inOrder(searchPromptView);

        presenter.showSearchPrompt();

        order.verify(searchPromptView).show();
        order.verifyNoMoreInteractions();

        presenter.showSearchPrompt();

        order.verify(searchPromptView).focus();
        order.verifyNoMoreInteractions();
    }

    @Test
    void stoppingSearchWhenNothingIsInProgressDoesNothing() {
        var presenter = createPresenter(createModel(0, 1, 2));

        presenter.stopSearch();

        verify(searchPromptView, never()).show();
        verifyNoInteractions(searchableView);
    }

    @Test
    void committingThePatternStartsSearch() {
        var presenter = createPresenter(createModel(0, 1, 2));

        presenter.showSearchPrompt();
        searchPromptView.commit("1");

        verify(searchPromptView).hide();
        verify(searchableView).showSearchResult(1);
    }

    @Test
    void committingUnsupportedPatternShowsError() {
        var presenter = createPresenter(createModel(0, 1, 2));

        presenter.showSearchPrompt();
        searchPromptView.commit("error");

        verify(searchPromptView, never()).hide();
        verify(searchPromptView).showPatternError(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void startingSearchInNonDefaultPositionWorks(int selectedRow) {
        withSelectedRow(selectedRow);
        var presenter = createPresenter(createModel(0, 1, 2));

        presenter.showSearchPrompt();
        searchPromptView.commit("even");

        verify(searchableView).showSearchResult(2);
    }

    @Test
    void startSearchShowsNoMatchIfPatternNotPresent() {
        var presenter = createPresenter(createModel(0, 1, 2));

        presenter.showSearchPrompt();
        searchPromptView.commit("5");

        verify(searchPromptView).hide();
        verify(statusPresenter).showNotFoundMessage();
    }

    @Test
    void searchNextShowsNext() {
        var presenter = createPresenter(createModel(0, 1, 2));
        withSearchStarted(presenter, "even");

        presenter.findNext();

        verify(searchableView).showSearchResult(2);
    }

    @Test
    void searchNextShowsNoResultsWhenExhausted() {
        var presenter = createPresenter(createModel(0, 1, 2));
        withSearchStarted(presenter, "odd");

        presenter.findNext();

        verify(statusPresenter).showNotFoundMessage();
    }

    @Test
    void searchPrevShowsPrev() {
        withSelectedRow(1);
        var presenter = createPresenter(createModel(0, 1, 2));
        withSearchStarted(presenter, "even");

        presenter.findPrev();

        verify(searchableView).showSearchResult(0);
    }

    @Test
    void searchPrevShowsNoResultsWhenExhausted() {
        withSelectedRow(1);
        var presenter = createPresenter(createModel(0, 1, 2));
        withSearchStarted(presenter, "odd");

        presenter.findPrev();

        verify(searchableView, never()).showSearchResult(any());
        verify(statusPresenter).showNotFoundMessage();
    }

    @Test
    void multipleFindNextWork() {
        withSelectedRow(0);
        var presenter = createPresenter(createModel(0, 1, 2, 3, 4, 5));
        withSearchStarted(presenter, "even");

        presenter.findNext();
        presenter.findNext();
        presenter.findNext();

        var order = inOrder(searchableView, statusPresenter);

        order.verify(searchableView).showSearchResult(2);
        order.verify(searchableView).showSearchResult(4);
        order.verify(statusPresenter).showNotFoundMessage();
        order.verifyNoMoreInteractions();
    }

    @Test
    void multipleFindPrevWork() {
        withSelectedRow(4);
        var presenter = createPresenter(createModel(0, 1, 2, 3, 4, 5));
        withSearchStarted(presenter, "even");

        presenter.findPrev();
        presenter.findPrev();
        presenter.findPrev();

        var order = inOrder(searchableView, statusPresenter);

        order.verify(searchableView).showSearchResult(2);
        order.verify(searchableView).showSearchResult(0);
        order.verify(statusPresenter).showNotFoundMessage();
        order.verifyNoMoreInteractions();
    }

    @Test
    void findNextPrevMixWorksAsIntended() {
        var presenter = createPresenter(createModel(0, 1, 2, 3, 4, 5));
        withSearchStarted(presenter, "even");

        presenter.findNext();
        presenter.findPrev();
        presenter.findPrev();
        presenter.findNext();

        var order = inOrder(searchableView, statusPresenter);

        order.verify(searchableView).showSearchResult(2);
        order.verify(searchableView).showSearchResult(0);
        order.verify(statusPresenter).showNotFoundMessage();
        order.verify(searchableView).showSearchResult(2);
        order.verifyNoMoreInteractions();
    }

    @Test
    void selectingMatchingRowMakesItNotSearchableForFindNext() {
        var presenter = createPresenter(createModel(0, 1, 2, 3, 4, 5));
        withSearchStarted(presenter, "even");

        searchableView.setSelectedRow(2);

        presenter.findNext();

        verify(searchableView).showSearchResult(4);
    }

    @Test
    void selectingMatchingRowMakesItNotSearchableForFindPrev() {
        var presenter = createPresenter(createModel(0, 1, 2, 3, 4, 5));
        withSearchStarted(presenter, "even");

        searchableView.setSelectedRow(2);

        presenter.findPrev();

        verify(searchableView).showSearchResult(0);
    }

    @Test
    void selectingNonMatchingRowMakesItSearchableForFindNext() {
        var presenter = createPresenter(createModel(0, 1, 2, 3, 4, 5));
        withSearchStarted(presenter, "even");

        searchableView.setSelectedRow(3);

        presenter.findNext();

        verify(searchableView).showSearchResult(4);
    }

    @Test
    void selectingNonMatchingRowMakesItSearchableForFindPrev() {
        var presenter = createPresenter(createModel(0, 1, 2, 3, 4, 5));
        withSearchStarted(presenter, "even");

        searchableView.setSelectedRow(3);

        presenter.findPrev();

        verify(searchableView).showSearchResult(2);
    }

    private SearchModel<Integer, Integer, Predicate<Integer>> createModel(int... values) {
        var model =
                new SearchModel<Integer, Integer, Predicate<Integer>>(new ListSearchModel<>(Ints.asList(values)));
        model.asSearchStrategyObservable().addObserver(modelObserver);
        return model;
    }

    private SearchPresenter<Integer, Integer, Predicate<Integer>> createPresenter(
            SearchModel<Integer, Integer, Predicate<Integer>> model) {
        return new SearchPresenter<>(new TestSequentialExecutor(MoreExecutors.directExecutor()), model, patternCompiler,
                searchableView,
                statusPresenter, searchPromptView);
    }

    private void withSelectedRow(int index) {
        searchableView.setSelectedRow(index);
    }

    private void withSearchStarted(SearchPresenter<?, ?, ?> presenter, String pattern) {
        presenter.showSearchPrompt();
        searchPromptView.commit(pattern);

        assertThat(searchPromptView.isShowing()).isFalse();
        reset(searchPromptView, searchableView);
    }

    private static Predicate<Integer> compilePredicate(String predicate) throws RequestCompilationException {
        return switch (predicate) {
            case "odd" -> i -> i % 2 == 1;
            case "even" -> i -> i % 2 == 0;
            default -> equalsToValue(predicate);
        };
    }

    private static Predicate<Integer> equalsToValue(String predicate) throws RequestCompilationException {
        try {
            var expected = Integer.valueOf(predicate);
            return i -> Objects.equals(expected, i);
        } catch (NumberFormatException e) {
            throw new RequestCompilationException("Failed to compile", predicate);
        }
    }
}

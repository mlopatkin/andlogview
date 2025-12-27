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

import name.mlopatkin.andlogview.AppExecutors;
import name.mlopatkin.andlogview.search.RequestCompilationException;
import name.mlopatkin.andlogview.search.Search;
import name.mlopatkin.andlogview.search.SearchModel;
import name.mlopatkin.andlogview.ui.status.SearchStatusPresenter;
import name.mlopatkin.andlogview.utils.MyFutures;
import name.mlopatkin.andlogview.widgets.DialogResult;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * The presenter for the full-text search functionality. The search GUI consists of two parts. The first, represented by
 * the {@link SearchableView} interface, is the data view that is being searched. When something is found, the view
 * takes care of showing the found item. The second, represented by the {@link SearchPromptView} interface, is the view
 * where the user can enter the search text. The text is then interpreted through the {@link SearchPatternCompiler}.
 * <p>
 * The presenter talks with the {@link SearchModel} to actually find stuff.
 *
 * @param <T> the underlying searchable item type
 * @param <P> the type of the item position
 * @param <S> the actual type of a predicate used to search the model.
 */
public class SearchPresenter<T, P, S extends Predicate<? super T>> {
    private final Executor uiThreadExecutor;
    private final SearchModel<T, P, S> searchModel;
    private final SearchPatternCompiler<S> patternCompiler;
    private final SearchableView<P> searchableView;
    private final SearchStatusPresenter searchStatusPresenter;
    private final SearchPromptView searchPromptView;

    public interface SearchableView<P> {
        void showSearchResult(P row);

        Optional<P> getSearchStartPosition();
    }

    /**
     * An interface to control the view where the user provides the search pattern. The View is initially hidden, but
     * appears when the pattern has to be entered. The view doesn't have to be modal. The user can commit the entered
     * pattern or dismiss the view. Upon commit, the validity of pattern is checked. If the pattern is invalid then the
     * view doesn't hide and the error is shown instead. Otherwise, the view hides and search starts. If the view is
     * dismissed, it just hides.
     * <p>
     * Note that the view doesn't hide itself, it is a responsibility of the presenter to drive the visibility state.
     */
    public interface SearchPromptView {
        /**
         * Shows the view to the user. The returned DialogResult can be used to obtain the entered pattern. The view
         * should not be shown if it is already showing.
         *
         * @return the promise of the dialog result.
         */
        DialogResult<String> show();

        /**
         * Focuses the view. The view must be already showing.
         */
        void focus();

        /**
         * Hides the view. Calling this method ends the current "session", the DialogResult from the last show will no
         * longer receive events. This method is a no-op if called upon the hidden view.
         */
        void hide();

        /**
         * Returns the current visibility of the view.
         *
         * @return {@code true} if the view is showing, {@code false} otherwise
         */
        boolean isShowing();

        /**
         * Clears the currently entered search pattern.
         */
        void clearSearchPattern();

        /**
         * Shows the error in pattern.
         *
         * @param errorMessage the error message to present to the user
         */
        void showPatternError(String errorMessage);
    }

    @Inject
    public SearchPresenter(
            @Named(AppExecutors.UI_EXECUTOR) Executor uiThreadExecutor,
            SearchModel<T, P, S> searchModel,
            SearchPatternCompiler<S> patternCompiler,
            SearchableView<P> searchableView,
            SearchStatusPresenter searchStatusPresenter,
            SearchPromptView searchPromptView) {
        this.uiThreadExecutor = uiThreadExecutor;
        this.searchModel = searchModel;
        this.patternCompiler = patternCompiler;
        this.searchableView = searchableView;
        this.searchStatusPresenter = searchStatusPresenter;
        this.searchPromptView = searchPromptView;
    }

    /**
     * Shows the search prompt and starts search when the user finishes entering the search pattern.
     */
    public void showSearchPrompt() {
        if (searchPromptView.isShowing()) {
            searchPromptView.focus();
            return;
        }

        searchStatusPresenter.reset();
        searchPromptView.show()
                .onCommit(this::tryStartSearchWithPrompt)
                .onDiscard(this::discardSearchPrompt);
    }

    /**
     * Finds the next encounter of the current search pattern. If there is no search in progress then opens a prompt for
     * the user to enter the pattern.
     */
    public void findNext() {
        find(Search.Direction.FORWARD);
    }

    /**
     * Finds the previous encounter of the current search pattern. If there is no search in progress then opens a prompt
     * for the user to enter the pattern.
     */
    public void findPrev() {
        find(Search.Direction.BACKWARD);
    }

    /**
     * Abandons current search.
     */
    public void stopSearch() {
        discardSearchPrompt();
    }

    private void find(Search.Direction direction) {
        searchStatusPresenter.reset();
        searchModel.getCurrentSearch().ifPresentOrElse(
                s -> continueSearch(direction, s),
                this::showSearchPrompt
        );
    }

    private void continueSearch(Search.Direction direction, Search<P> search) {
        searchableView.getSearchStartPosition().ifPresent(search::setPosition);
        search.search(direction)
                .thenAcceptAsync(
                        r -> r.ifPresentOrElse(this::onSearchHit, this::onNoMoreHits),
                        uiThreadExecutor
                ).exceptionally(MyFutures::uncaughtException);
    }

    private void onSearchHit(P foundPosition) {
        searchableView.showSearchResult(foundPosition);
    }

    private void onNoMoreHits() {
        searchStatusPresenter.showNotFoundMessage();
    }

    private void tryStartSearchWithPrompt(String patternText) {
        try {
            S pattern = patternCompiler.compile(patternText);
            searchPromptView.hide();
            var startPosition = searchableView.getSearchStartPosition();
            startPosition.ifPresentOrElse(
                    p -> searchModel.startSearch(pattern, p),
                    () -> searchModel.startSearch(pattern)
            );
            find(Search.Direction.FORWARD.alsoSearchCurrent());
        } catch (RequestCompilationException e) {
            searchPromptView.showPatternError(
                    String.format("'%s' isn't a valid search expression. %s", patternText, e.getMessage()));
        }
    }

    private void discardSearchPrompt() {
        searchStatusPresenter.reset();
        searchPromptView.hide();
        searchPromptView.clearSearchPattern();
        searchModel.finishSearch();
    }
}

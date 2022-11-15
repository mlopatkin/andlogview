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

package name.mlopatkin.andlogview.search;

import name.mlopatkin.andlogview.utils.events.Observable;
import name.mlopatkin.andlogview.utils.events.Subject;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * A model for the full-text search. This class is stateful. It can only perform one search at a time.
 *
 * @param <T> the type of items
 * @param <S> the subtype of the predicate to perform full-text search on the item
 */
public class SearchModel<T, P, S extends Predicate<? super T>> {
    /**
     * An observer of the currently active search strategy.
     *
     * @param <S> the subtype of the predicate to perform full-text search on the item
     */
    public interface StrategyObserver<S> {
        /**
         * Called when the new search is started.
         *
         * @param searchStrategy the new search strategy
         */
        void onNewSearchStrategy(S searchStrategy);

        /**
         * Called when the search is finished. This method is not called if another search replaces the current search.
         */
        void onSearchStrategyCleared();
    }

    private final Subject<StrategyObserver<S>> strategyObservers = new Subject<>();

    private final SearchDataModel<T, P> dataModel;

    private @Nullable SearchImpl<T, P, S> currentSearch;

    /**
     * Constructs the new search model
     *
     * @param dataModel the data model providing data to search in
     */
    public SearchModel(SearchDataModel<T, P> dataModel) {
        this.dataModel = dataModel;
    }

    /**
     * Starts the new search session starting at {@code startPosition}. The ongoing search can then be obtained with
     * {@link #getCurrentSearch()}. If there was an ongoing search already, it is cancelled. The item at the
     * {@code startPosition} is included into search.
     *
     * @param searchStrategy the search strategy
     * @param startPosition the position to start search at
     */
    public void startSearch(S searchStrategy, P startPosition) {
        var cursor = dataModel.newCursor();
        cursor.setPosition(startPosition);

        startSearch(cursor, searchStrategy);
    }

    /**
     * Starts the new search session. The ongoing search can then be obtained with {@link #getCurrentSearch()}. If
     * there was an ongoing search already, it is cancelled.
     *
     * @param searchStrategy the search strategy
     */
    public void startSearch(S searchStrategy) {
        startSearch(dataModel.newCursor(), searchStrategy);
    }

    private void startSearch(SearchCursor<T, P> cursor, S searchStrategy) {
        currentSearch = new SearchImpl<>(cursor, searchStrategy);
        for (var o : strategyObservers) {
            o.onNewSearchStrategy(searchStrategy);
        }
    }

    /**
     * Finishes the ongoing search if any.
     */
    public void finishSearch() {
        if (currentSearch != null) {
            currentSearch = null;
            for (var o : strategyObservers) {
                o.onSearchStrategyCleared();
            }
        }
    }

    /**
     * Returns the strategy used for the ongoing search.
     *
     * @return the strategy of the ongoing search or empty optional if no search is in progress
     */
    public Optional<S> getSearchStrategy() {
        return Optional.ofNullable(currentSearch).map(SearchImpl::getStrategy);
    }

    /**
     * Returns the observable to subscribe for the search strategy changes.
     *
     * @return the observable
     */
    public Observable<StrategyObserver<S>> asSearchStrategyObservable() {
        return strategyObservers.asObservable();
    }

    /**
     * Returns the current ongoing search.
     *
     * @return the ongoing search or empty optional if no search is in progress
     */
    public Optional<Search<P>> getCurrentSearch() {
        return Optional.ofNullable(currentSearch);
    }
}

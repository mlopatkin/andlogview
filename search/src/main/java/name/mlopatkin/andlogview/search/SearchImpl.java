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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * An implementation of {@link Search} that provides access to the strategy.
 */
class SearchImpl<T, P, S extends Predicate<? super T>> implements Search<P> {
    private final SyncSearch<?, P> search;
    private final S strategy;

    SearchImpl(SearchCursor<T, P> searchCursor, S strategy) {
        this.strategy = strategy;
        this.search = new SyncSearch<>(searchCursor, strategy);
    }

    @Override
    public CompletableFuture<Optional<P>> search(Direction direction) {
        return CompletableFuture.completedFuture(search.search(direction));
    }

    @Override
    public void setPosition(P position) {
        search.setPosition(position);
    }

    public S getStrategy() {
        return strategy;
    }
}

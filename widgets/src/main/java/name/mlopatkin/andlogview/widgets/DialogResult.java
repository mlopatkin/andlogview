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

package name.mlopatkin.andlogview.widgets;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * The asynchronous result of some dialog or dialog-like input. It is similar to {@link CompletableFuture}, but, unlike
 * the latter, it can deliver result multiple times. The original use case is to allow result consumers to verify the
 * validity of the result and allow dialog to close or show an error and wait for a new result.
 *
 * @param <T> the type of the result
 */
public final class DialogResult<T> {
    private final List<Consumer<? super T>> commitActions = new ArrayList<>(1);
    private final List<Runnable> discardActions = new ArrayList<>(1);

    private DialogResult() {}

    /**
     * Adds an action to be invoked on commit.
     *
     * @param action the action
     * @return this DialogResult
     */
    @CanIgnoreReturnValue
    public DialogResult<T> onCommit(Consumer<? super T> action) {
        commitActions.add(action);
        return this;
    }

    /**
     * Adds an action to be invoked on discard.
     *
     * @param action the action
     * @return this DialogResult
     */
    @CanIgnoreReturnValue
    public DialogResult<T> onDiscard(Runnable action) {
        discardActions.add(action);
        return this;
    }

    /**
     * A {@code Subject} that is used to provide result into the DialogResult.
     *
     * @param <T> the type of the result
     */
    public static class DialogSubject<T> {
        private final DialogResult<T> handler = new DialogResult<>();

        /**
         * Commits the result. Can be called multiple times.
         *
         * @param result the result to commit
         */
        public void commit(T result) {
            for (var action : handler.commitActions) {
                action.accept(result);
            }

        }

        /**
         * Discards the result. Can be called multiple times.
         */
        public void discard() {
            for (var action : handler.discardActions) {
                action.run();
            }
        }

        /**
         * Returns the Result instance for clients to subscribe to.
         *
         * @return the DialogResult
         */
        public DialogResult<T> asResult() {
            return handler;
        }
    }
}

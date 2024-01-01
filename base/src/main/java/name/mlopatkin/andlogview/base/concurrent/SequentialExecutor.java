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

package name.mlopatkin.andlogview.base.concurrent;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * An {@link Executor} that only runs its callbacks sequentially. Swing's EDT and
 * {@link Executors#newSingleThreadExecutor()} are an example of such executors.
 * <p>
 * Note that the direct executor is not sequential (tasks submitted by different threads may run concurrently).
 * Decorating the direct executor may have unwanted side effects of a task running on an unexpected thread or deadlocks.
 */
public interface SequentialExecutor extends Executor {

    /**
     * Creates asequential executor that executes work on UI thread. It considers any UI callbacks to be "on sequence"
     * too.
     *
     * @return the UI-thread bound SequentialExecutor.
     */
    static SequentialExecutor edt() {
        return new UiExecutor();
    }

    /**
     * Creates new SequentialExecutor that uses the delegate to actually run the tasks.
     *
     * @param delegate the delegate to run tasks
     * @return the SequentialExecutor
     */
    static SequentialExecutor decorate(Executor delegate) {
        return new DelegatingExecutor(MoreExecutors.newSequentialExecutor(delegate));
    }

    /**
     * Returns true if the current thread currently runs this executor's callback.
     *
     * @return true if we're executing the sequence.
     */
    boolean isOnSequence();

    /**
     * Throws an exception if the call is not on this executor.
     *
     * @throws IllegalStateException if the current thread doesn't execute this executor's task.
     */
    default void checkSequence() {
        Preconditions.checkState(isOnSequence(), "Not on sequence");
    }
}

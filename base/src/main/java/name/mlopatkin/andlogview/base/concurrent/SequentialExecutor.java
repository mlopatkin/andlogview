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

import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * An {@link Executor} that only runs its callbacks sequentially. Swing's EDT and
 * {@link Executors#newSingleThreadExecutor()} are an example of such executors.
 */
public interface SequentialExecutor extends Executor {

    static SequentialExecutor edt() {
        return new UiExecutor();
    }

    static SequentialExecutor singleThread() {
        return decorate(Executors.newSingleThreadExecutor());
    }

    static SequentialExecutor singleThread(ThreadFactory threadFactory) {
        return decorate(Executors.newSingleThreadExecutor(threadFactory));
    }

    static SequentialExecutor direct() {
        return decorate(MoreExecutors.directExecutor());
    }

    static SequentialExecutor decorate(Executor other) {
        return new DelegatingExecutor(other);
    }

    /**
     * Throws an exception if the call is not on this executor.
     *
     * @throws IllegalStateException if the current thread doesn't execute this executor's task.
     */
    void checkSequence();
}

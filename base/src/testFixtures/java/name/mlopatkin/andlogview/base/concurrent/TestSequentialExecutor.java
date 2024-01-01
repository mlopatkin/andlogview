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

package name.mlopatkin.andlogview.base.concurrent;

import java.util.concurrent.Executor;

/**
 * A sequential executor that always considers the thread on which it was created "on sequence". Typically, used in test
 * code, where you don't want to wrap sequence-bound code invoked by the test itself with {@code execute(() -> {})}.
 */
public class TestSequentialExecutor implements SequentialExecutor {
    private final ThreadLocal<Boolean> isOnSequence = ThreadLocal.withInitial(() -> false);
    private final Executor delegate;

    public TestSequentialExecutor(Executor delegate) {
        this.delegate = delegate;
        isOnSequence.set(true);
    }

    @Override
    public boolean isOnSequence() {
        return isOnSequence.get();
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(() -> {
            var oldValue = isOnSequence.get();
            isOnSequence.set(true);
            try {
                command.run();
            } finally {
                if (oldValue) {
                    isOnSequence.set(true);
                } else {
                    isOnSequence.remove();
                }
            }
        });
    }
}

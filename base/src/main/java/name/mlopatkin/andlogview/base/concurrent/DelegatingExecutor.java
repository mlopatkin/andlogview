/*
 * Copyright 2023 the Andlogview authors
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

class DelegatingExecutor implements SequentialExecutor {
    private final ThreadLocal<Boolean> isRunning = ThreadLocal.withInitial(() -> false);
    private final Executor delegate;

    public DelegatingExecutor(Executor delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isOnSequence() {
        return isRunning.get();
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(() -> {
            var prevValue = isRunning.get();
            isRunning.set(true);
            try {
                command.run();
            } finally {
                if (prevValue) {
                    isRunning.set(true);
                } else {
                    isRunning.remove();
                }
            }
        });
    }
}

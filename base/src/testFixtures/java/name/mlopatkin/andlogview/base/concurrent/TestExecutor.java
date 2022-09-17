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

import com.google.errorprone.annotations.concurrent.GuardedBy;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * A test executor that collects all submitted commands but doesn't run them until {@link #flush()} is called.
 */
public class TestExecutor implements Executor {
    private final Object lock = new Object();

    @GuardedBy("lock")
    private final ArrayDeque<Runnable> commands = new ArrayDeque<>();

    @Override
    public void execute(@NonNull Runnable command) {
        synchronized (lock) {
            commands.add(Objects.requireNonNull(command));
        }
    }

    /**
     * Synchronously executes submitted runnables until the queue is empty. If the runnables post more work, this work
     * is also executed.
     */
    public void flush() {
        Optional<Runnable> command;
        do {
            command = pop();
            command.ifPresent(Runnable::run);
        } while (command.isPresent());
    }

    private Optional<Runnable> pop() {
        synchronized (lock) {
            return Optional.ofNullable(commands.pollFirst());
        }
    }
}

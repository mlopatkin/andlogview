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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.MoreExecutors;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

class DelegatingExecutorTest {
    @Test
    void executorIsReenterable() {
        var executor = new DelegatingExecutor(MoreExecutors.directExecutor());
        Runnable command = mock();

        executor.execute(() -> executor.execute(command));

        verify(command, only()).run();
    }

    @Test
    void executorIsReenterableMultipleTimes() {
        var executor = new DelegatingExecutor(MoreExecutors.directExecutor());
        Runnable command = mock();

        executor.execute(() -> {
            executor.execute(command);
            executor.execute(command);
        });

        verify(command, times(2)).run();
    }

    @Test
    void executorCheckPassesAfterReenterableCall() {
        var executor = new DelegatingExecutor(MoreExecutors.directExecutor());
        var wasOnSequence = new AtomicBoolean();

        executor.execute(() -> {
            executor.execute(() -> {});
            wasOnSequence.set(executor.isOnSequence());
        });

        assertThat(wasOnSequence).isTrue();
    }
}

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

package name.mlopatkin.andlogview.utils.events;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import name.mlopatkin.andlogview.base.concurrent.TestExecutor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

@ExtendWith(MockitoExtension.class)
class ThreadSafeSubjectTest {
    @Mock
    Consumer<String> mockObserver;

    @Test
    void unregisteredObserversGetNoNotifications() {
        var executor = new TestExecutor();
        var subject = createSubject();

        Mockito.doAnswer(invocation -> {
            subject.asObservable().removeObserver(mockObserver);
            return null;
        }).when(mockObserver).accept("first");

        subject.asObservable().addObserver(mockObserver, executor);
        subject.forEach(obs -> obs.accept("first"));
        subject.forEach(obs -> obs.accept("second"));

        executor.flush();

        verify(mockObserver).accept("first");
        verify(mockObserver, never()).accept("second");
    }

    private ThreadSafeSubject<Consumer<String>> createSubject() {
        return new ThreadSafeSubject<>();
    }
}

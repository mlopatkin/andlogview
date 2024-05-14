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

package name.mlopatkin.andlogview.utils.events;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

class CompoundObservableTest {
    private final Runnable observer = mock();

    private final Subject<Runnable> first = new Subject<>();
    private final Subject<Runnable> second = new Subject<>();

    @Test
    void receivesNotificationsFromFirstSubject() {
        var compound = createCompound();
        compound.addObserver(observer);

        notifyObservers(first);

        verify(observer).run();
    }

    @Test
    void receivesNotificationsFromSecondSubject() {
        var compound = createCompound();
        compound.addObserver(observer);

        notifyObservers(second);

        verify(observer).run();
    }

    @Test
    void unsubscribesFromBothSubjects() {
        var compound = createCompound();
        compound.addObserver(observer);
        compound.removeObserver(observer);

        notifyObservers(first);
        notifyObservers(second);

        verify(observer, never()).run();
    }

    private void notifyObservers(Subject<Runnable> subject) {
        subject.forEach(Runnable::run);
    }

    private CompoundObservable<Runnable> createCompound() {
        return new CompoundObservable<>(first.asObservable(), second.asObservable());
    }
}

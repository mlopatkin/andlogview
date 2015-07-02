/*
 * Copyright 2015 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.utils.events;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class SubjectTest {

    public interface TestObserver {
        void onEvent();
    }

    Subject<TestObserver> subject = new Subject<>();

    private void notifyObservers() {
        for (TestObserver o : subject) {
            o.onEvent();
        }
    }

    @Test
    public void testAddObserver() throws Exception {
        TestObserver o = mock(TestObserver.class);

        subject.asObservable().addObserver(o);
        notifyObservers();

        verify(o).onEvent();
    }

    @Test
    public void testRemoveObserver() throws Exception {
        TestObserver o = mock(TestObserver.class);

        subject.asObservable().addObserver(o);
        subject.asObservable().removeObserver(o);
        notifyObservers();

        verify(o, never()).onEvent();
    }

    @Test
    public void testDoubleAddObserver() throws Exception {
        TestObserver o = mock(TestObserver.class);

        subject.asObservable().addObserver(o);
        subject.asObservable().addObserver(o);
        notifyObservers();

        verify(o).onEvent();
    }

    @Test
    public void testDoubleAddAndRemoveObserver() throws Exception {
        TestObserver o = mock(TestObserver.class);

        subject.asObservable().addObserver(o);
        subject.asObservable().addObserver(o);
        subject.asObservable().removeObserver(o);
        notifyObservers();

        verify(o, never()).onEvent();
    }


    @Test
    public void testRemoveNull() throws Exception {
        TestObserver o = mock(TestObserver.class);

        subject.asObservable().addObserver(o);
        subject.asObservable().removeObserver(null);

        notifyObservers();

        verify(o).onEvent();
    }

    @Test
    public void testRemoveUnregistered() throws Exception {
        TestObserver o = mock(TestObserver.class);

        subject.asObservable().removeObserver(o);

        notifyObservers();

        verify(o, never()).onEvent();
    }

    @Test
    public void testAddTwoRemoveOne() throws Exception {
        TestObserver o = mock(TestObserver.class);

        subject.asObservable().addObserver(o);
        subject.asObservable().removeObserver(null);

        notifyObservers();

        verify(o).onEvent();
    }
}

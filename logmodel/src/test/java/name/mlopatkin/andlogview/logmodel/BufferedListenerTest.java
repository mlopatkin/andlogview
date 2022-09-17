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

package name.mlopatkin.andlogview.logmodel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import name.mlopatkin.andlogview.base.concurrent.TestExecutor;

import com.google.common.util.concurrent.MoreExecutors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Executor;

@ExtendWith(MockitoExtension.class)
class BufferedListenerTest {
    @Mock
    private BatchRecordsReceiver<Integer> receiver;

    @Test
    void singleRecordIsDelivered() {
        BufferedListener<Integer> listener = createListener();
        listener.addRecord(1);

        verify(receiver, only()).addRecords(singletonList(1));
    }

    @Test
    void multipleRecordsAreDeliveredOneByOne() {
        BufferedListener<Integer> listener = createListener();
        listener.addRecord(1);
        listener.addRecord(2);
        listener.addRecord(3);

        InOrder order = inOrder(receiver);
        order.verify(receiver).addRecords(singletonList(1));
        order.verify(receiver).addRecords(singletonList(2));
        order.verify(receiver).addRecords(singletonList(3));
        order.verifyNoMoreInteractions();
    }

    @Test
    void recordsAreCollectedUntilExecutorIsFlushed() {
        TestExecutor testExecutor = new TestExecutor();
        BufferedListener<Integer> listener = createListener(testExecutor);
        listener.addRecord(1);
        listener.addRecord(2);

        verify(receiver, never()).addRecords(any());

        testExecutor.flush();

        verify(receiver, only()).addRecords(asList(1, 2));
    }

    @Test
    void setRecordsAreDeliveredAsSetRecords() {
        BufferedListener<Integer> listener = createListener();
        listener.setRecords(asList(1, 2, 3));

        verify(receiver, only()).setRecords(asList(1, 2, 3));
    }

    @Test
    void setRecordsAreDeliveredOnExecutor() {
        TestExecutor testExecutor = new TestExecutor();
        BufferedListener<Integer> listener = createListener(testExecutor);
        listener.setRecords(asList(1, 2, 3));

        verify(receiver, never()).setRecords(any());

        testExecutor.flush();
        verify(receiver, only()).setRecords(asList(1, 2, 3));
    }


    private BufferedListener<Integer> createListener() {
        return new BufferedListener<>(receiver, MoreExecutors.directExecutor());
    }

    private BufferedListener<Integer> createListener(Executor executor) {
        return new BufferedListener<>(receiver, executor);
    }
}

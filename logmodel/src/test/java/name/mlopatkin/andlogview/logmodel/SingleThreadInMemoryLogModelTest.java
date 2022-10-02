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

import static name.mlopatkin.andlogview.logmodel.AssertLogModel.assertThat;
import static name.mlopatkin.andlogview.logmodel.AssertLogRecord.assertThatRecord;
import static name.mlopatkin.andlogview.logmodel.LogRecordUtils.logRecord;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.stream.Collectors;

class SingleThreadInMemoryLogModelTest {

    @Test
    void createdLogModelIsEmpty() {
        SingleThreadInMemoryLogModel model = createModel();

        assertThat(model).isEmpty();
    }

    @Test
    void logModelThrowsWhenGetRecordOutsideOfBounds() {
        SingleThreadInMemoryLogModel model = createModel();

        assertThatThrownBy(() -> model.getAt(0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> model.getAt(5)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> model.getAt(-1)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void addedRecordIsAvailable() {
        SingleThreadInMemoryLogModel model = createModel();

        addRecords(model, logRecord("record1").withTime("01-01 10:00:00.000"));

        assertThat(model).hasSize(1);
        assertThatRecord(model.getAt(0)).hasMessage("record1");
    }

    @Test
    void recordsAreProperlyMerged() {
        SingleThreadInMemoryLogModel model = createModel();

        addRecords(
                model,
                logRecord("record1").withTime("01-01 10:00:10.000"),
                logRecord("record2").withTime("01-01 10:00:30.000"));

        addRecords(
                model,
                logRecord("record begin").withTime("01-01 10:00:00.000"),
                logRecord("record middle").withTime("01-01 10:00:15.000"),
                logRecord("record end").withTime("01-01 10:00:59.000"));

        assertThat(model).hasSize(5);
        assertThatRecord(model.getAt(0)).hasMessage("record begin");
        assertThatRecord(model.getAt(1)).hasMessage("record1");
        assertThatRecord(model.getAt(2)).hasMessage("record middle");
        assertThatRecord(model.getAt(3)).hasMessage("record2");
        assertThatRecord(model.getAt(4)).hasMessage("record end");
    }

    @Test
    void observerIsNotifiedAboutFirstRecord() {
        SingleThreadInMemoryLogModel model = createModel();
        LogModel.Observer observer = createObserver();
        doAnswer(invocation -> assertThat(model).hasSize(1)).when(observer).onRecordsInserted(anyInt(), anyInt());
        model.asObservable().addObserver(observer);

        addRecords(model, logRecord("record1").withTime("01-01 10:00:00.000"));

        verify(observer).onRecordsInserted(0, 1);
    }

    @Test
    void observerIsNotifiedAboutRecordAddedToBeginning() {
        SingleThreadInMemoryLogModel model = createModel();
        LogModel.Observer observer = createObserver();
        addRecords(model, logRecord("record1").withTime("01-01 10:00:00.000"));
        doAnswer(invocation -> assertThat(model).hasSize(2)).when(observer).onRecordsInserted(anyInt(), anyInt());

        model.asObservable().addObserver(observer);
        addRecords(model, logRecord("record0").withTime("01-01 09:00:00.000"));

        verify(observer).onRecordsInserted(0, 1);
    }

    @Test
    void observerIsNotifiedAboutRecordAddedToEnd() {
        SingleThreadInMemoryLogModel model = createModel();
        LogModel.Observer observer = createObserver();
        doAnswer(invocation -> assertThat(model).hasSize(2)).when(observer).onRecordsInserted(anyInt(), anyInt());
        addRecords(model, logRecord("record1").withTime("01-01 10:00:00.000"));

        model.asObservable().addObserver(observer);
        addRecords(model, logRecord("record2").withTime("01-01 11:00:00.000"));

        verify(observer).onRecordsInserted(1, 1);
    }

    @Test
    void observerIsNotifiedAboutRecordAddedToMiddle() {
        SingleThreadInMemoryLogModel model = createModel();
        LogModel.Observer observer = createObserver();
        doAnswer(invocation -> assertThat(model).hasSize(3)).when(observer).onRecordsInserted(anyInt(), anyInt());
        addRecords(
                model,
                logRecord("record1").withTime("01-01 10:00:00.000"),
                logRecord("record2").withTime("01-01 12:00:00.000"));

        model.asObservable().addObserver(observer);
        addRecords(model, logRecord("record0_5").withTime("01-01 11:00:00.000"));

        verify(observer).onRecordsInserted(1, 1);
    }

    @Test
    void observerIsNotifiedBeforeRecordsAreInserted() {
        SingleThreadInMemoryLogModel model = createModel();
        LogModel.Observer observer = createObserver();
        doAnswer(invocation -> assertThat(model).hasSize(2)).when(observer).onBeforeRecordsInserted();
        addRecords(
                model,
                logRecord("record1").withTime("01-01 10:00:00.000"),
                logRecord("record2").withTime("01-01 12:00:00.000"));

        model.asObservable().addObserver(observer);
        addRecords(model, logRecord("record3").withTime("01-01 14:00:00.000"));

        InOrder order = inOrder(observer);
        order.verify(observer).onBeforeRecordsInserted();
        order.verify(observer).onRecordsInserted(2, 1);
    }

    @Test
    void clearMethodDiscardsEntries() {
        SingleThreadInMemoryLogModel model = createModel();
        addRecords(model,
                logRecord("record1").withTime("01-01 10:00:00.000"),
                logRecord("record2").withTime("01-01 12:00:00.000"));

        model.clear();

        assertThat(model).isEmpty();
    }

    @Test
    void canClearEmptyModel() {
        SingleThreadInMemoryLogModel model = createModel();

        model.clear();

        assertThat(model).isEmpty();
    }

    @Test
    void observerIsNotifiedWhenEntriesDiscarded() {
        SingleThreadInMemoryLogModel model = createModel();
        LogModel.Observer observer = createObserver();
        doAnswer(invocation -> assertThat(model).isEmpty()).when(observer).onRecordsDiscarded(anyInt());

        addRecords(
                model,
                logRecord("record1").withTime("01-01 10:00:00.000"),
                logRecord("record2").withTime("01-01 12:00:00.000"));

        model.asObservable().addObserver(observer);
        model.clear();

        verify(observer, only()).onRecordsDiscarded(2);
    }

    @Test
    void observerIsNotifiedWhenEmptyModelIsCleared() {
        SingleThreadInMemoryLogModel model = createModel();
        LogModel.Observer observer = createObserver();
        doAnswer(invocation -> assertThat(model).isEmpty()).when(observer).onRecordsDiscarded(anyInt());

        model.asObservable().addObserver(observer);
        model.clear();

        verify(observer, only()).onRecordsDiscarded(0);
    }

    @Test
    void settingRecordsReplacesContent() {
        SingleThreadInMemoryLogModel model = createModel();
        addRecords(
                model,
                logRecord("record1").withTime("01-01 10:00:00.000"),
                logRecord("record2").withTime("01-01 12:00:00.000"));

        setRecords(model, logRecord("record0"));

        assertThat(model).hasSize(1).hasRecordWithMessageAt(0, "record0");
    }

    @Test
    void settingRecordsNotifiesListeners() {
        SingleThreadInMemoryLogModel model = createModel();
        LogModel.Observer observer = createObserver();
        doAnswer(invocation -> assertThat(model).isEmpty()).when(observer).onRecordsDiscarded(anyInt());
        doAnswer(invocation -> assertThat(model).isEmpty()).when(observer).onBeforeRecordsInserted();
        doAnswer(invocation -> assertThat(model).hasSize(1)).when(observer).onRecordsInserted(anyInt(), anyInt());
        addRecords(
                model,
                logRecord("record1").withTime("01-01 10:00:00.000"),
                logRecord("record2").withTime("01-01 12:00:00.000"));


        model.asObservable().addObserver(observer);
        setRecords(model, logRecord("record0"));

        InOrder order = inOrder(observer);
        order.verify(observer).onRecordsDiscarded(2);
        order.verify(observer).onBeforeRecordsInserted();
        order.verify(observer).onRecordsInserted(0, 1);
        order.verifyNoMoreInteractions();
    }

    private SingleThreadInMemoryLogModel createModel() {
        return new SingleThreadInMemoryLogModel();
    }

    private LogModel.Observer createObserver() {
        return Mockito.mock(LogModel.Observer.class);
    }

    private void addRecords(SingleThreadInMemoryLogModel model, LogRecordBuilder... recordBuilders) {
        model.addRecords(Arrays.stream(recordBuilders)
                .map(LogRecordBuilder::build)
                .collect(Collectors.toList()));
    }

    private void setRecords(SingleThreadInMemoryLogModel model, LogRecordBuilder... recordBuilders) {
        model.setRecords(Arrays.stream(recordBuilders)
                .map(LogRecordBuilder::build)
                .collect(Collectors.toList()));
    }
}

/*
 * Copyright 2018 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.mainframe;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.bitbucket.mlopatkin.android.logviewer.config.ConfigStorage;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class UserColumnOrderTest {

    private final Gson gson = new Gson();

    @Test
    public void createdOrderIsConsistentWithInitialArray() {
        UserColumnOrder order = makeOrder(Column.INDEX, Column.TAG, Column.PID);

        assertTrue(order.compare(Column.INDEX, Column.INDEX) == 0);
        assertTrue(order.compare(Column.TAG, Column.TAG) == 0);
        assertTrue(order.compare(Column.PID, Column.PID) == 0);

        assertTrue(order.compare(Column.INDEX, Column.TAG) < 0);
        assertTrue(order.compare(Column.TAG, Column.INDEX) > 0);

        assertTrue(order.compare(Column.INDEX, Column.PID) < 0);
        assertTrue(order.compare(Column.PID, Column.INDEX) > 0);

        assertTrue(order.compare(Column.TAG, Column.PID) < 0);
        assertTrue(order.compare(Column.PID, Column.TAG) > 0);
    }

    @Test
    public void setColumnBeforeMovesColumn() {
        UserColumnOrder order = makeOrder(Column.INDEX, Column.TAG, Column.PID);

        order.setColumnBefore(Column.PID, Column.TAG);

        assertTrue(order.compare(Column.INDEX, Column.INDEX) == 0);
        assertTrue(order.compare(Column.TAG, Column.TAG) == 0);
        assertTrue(order.compare(Column.PID, Column.PID) == 0);

        // INDEX PID TAG now
        assertTrue(order.compare(Column.INDEX, Column.TAG) < 0);
        assertTrue(order.compare(Column.TAG, Column.INDEX) > 0);

        assertTrue(order.compare(Column.INDEX, Column.PID) < 0);
        assertTrue(order.compare(Column.PID, Column.INDEX) > 0);

        assertTrue(order.compare(Column.PID, Column.TAG) < 0);
        assertTrue(order.compare(Column.TAG, Column.PID) > 0);
    }

    @Test
    public void setColumnBeforeNullMovesColumnToTheEnd() {
        UserColumnOrder order = makeOrder(Column.INDEX, Column.TAG, Column.PID);

        order.setColumnBefore(Column.TAG, null);

        assertTrue(order.compare(Column.INDEX, Column.INDEX) == 0);
        assertTrue(order.compare(Column.TAG, Column.TAG) == 0);
        assertTrue(order.compare(Column.PID, Column.PID) == 0);

        // INDEX PID TAG now
        assertTrue(order.compare(Column.INDEX, Column.TAG) < 0);
        assertTrue(order.compare(Column.TAG, Column.INDEX) > 0);

        assertTrue(order.compare(Column.INDEX, Column.PID) < 0);
        assertTrue(order.compare(Column.PID, Column.INDEX) > 0);

        assertTrue(order.compare(Column.PID, Column.TAG) < 0);
        assertTrue(order.compare(Column.TAG, Column.PID) > 0);
    }

    @Test
    public void setColumnBeforeInvokesCallback() {
        Runnable callback = mock(Runnable.class);
        UserColumnOrder order = new UserColumnOrder(Arrays.asList(Column.INDEX, Column.TAG, Column.PID), callback);

        order.setColumnBefore(Column.PID, Column.TAG);

        verify(callback).run();
    }

    private static UserColumnOrder makeOrder(Column... columns) {
        return new UserColumnOrder(Arrays.asList(columns), () -> {
        });
    }

    @Test
    public void validJsonValueIsParseable() throws Exception {
        List<Column> columns =
                UserColumnOrder.CLIENT.fromJson(gson, formatOrderAsJsonArray(Arrays.stream(Column.values())));

        assertEquals(Arrays.asList(Column.values()), columns);
    }

    @Test(expected = ConfigStorage.InvalidJsonContentException.class)
    public void missingValueCausesExceptionWhenParsing() throws Exception {
        UserColumnOrder.CLIENT.fromJson(gson, formatOrderAsJsonArray(Arrays.stream(Column.values()).skip(1)));
    }

    @Test(expected = ConfigStorage.InvalidJsonContentException.class)
    public void duplicateValueCausesExceptionWhenParsing() throws Exception {
        UserColumnOrder.CLIENT.fromJson(gson, formatOrderAsJsonArray(
                Stream.concat(Arrays.stream(Column.values()), Stream.of(Column.APP_NAME))));
    }

    private static JsonElement formatOrderAsJsonArray(Stream<Column> columns) {
        return new JsonParser().parse(columns.map(Column::name).collect(Collectors.joining(", ", "[", "]")));
    }
}

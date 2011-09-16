/*
 * Copyright 2011 Mikhail Lopatkin
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
package org.bitbucket.mlopatkin.utils;

import static org.bitbucket.mlopatkin.utils.ListTestUtils.list;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class MyListUtilsUpperBoundTest {

    @Test
    public void testGetUpperBoundPos_emptyList() {
        List<Integer> items = list();
        int value = 1;
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(0, result);
    }

    @Test
    public void testGetUpperBoundPos_oneItemListBefore() {
        List<Integer> items = list(2);
        int value = 1;
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(0, result);
    }

    @Test
    public void testGetUpperBoundPos_oneItemListMatch() {
        List<Integer> items = list(1);
        int value = 1;
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(1, result);
    }

    @Test
    public void testGetUpperBoundPos_oneItemListAfter() {
        List<Integer> items = list(0);
        int value = 1;
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(1, result);
    }

    @Test
    public void testGetUpperBoundPos_twoItemsListBefore() {
        List<Integer> items = list(1, 3);
        int value = 0;
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(0, result);
    }

    @Test
    public void testGetUpperBoundPos_twoItemsListBetween() {
        List<Integer> items = list(1, 3);
        int value = 2;
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(1, result);
    }

    @Test
    public void testGetUpperBoundPos_twoItemsListAfter() {
        List<Integer> items = list(1, 3);
        int value = 4;
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(2, result);
    }

    @Test
    public void testGetUpperBoundPos_twoItemsListMatchFirst() {
        List<Integer> items = list(1, 3);
        int value = 1;
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(1, result);
    }

    @Test
    public void testGetUpperBoundPos_twoItemsListMatchSecond() {
        List<Integer> items = list(1, 3);
        int value = 3;
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(2, result);
    }

    @Test
    public void testGetUpperBoundPos_twoItemsListMatchBoth() {
        List<Integer> items = list(1, 1);
        int value = 1;
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(2, result);
    }

    @Test
    public void testGetUpperBoundPos_manyItemsInTheMiddle() {
        List<Integer> items = list(1, 2, 3, 4, 6, 7, 8, 9);
        int value = 5;
        int expected = reliableGetUpperBound(items, value);
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUpperBoundPos_manyItemsIntervalInTheMiddle() {
        List<Integer> items = list(1, 2, 3, 4, 4, 6, 6, 7, 8, 9);
        int value = 5;
        int expected = reliableGetUpperBound(items, value);
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUpperBoundPos_manyItemsIntervalMatchInTheMiddle() {
        List<Integer> items = list(1, 2, 3, 4, 4, 5, 5, 5, 5, 5, 6, 6, 7, 8, 9);
        int value = 5;
        int expected = reliableGetUpperBound(items, value);
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUpperBoundPos_manyItemsIntervalMatchInTheEnd() {
        List<Integer> items = list(1, 2, 3, 4, 4, 5, 5, 5, 5, 5);
        int value = 5;
        int expected = reliableGetUpperBound(items, value);
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUpperBoundPos_manyItemsIntervalMatchInTheBeginning() {
        List<Integer> items = list(5, 5, 5, 5, 5, 6, 6, 7, 8, 9);
        int value = 5;
        int expected = reliableGetUpperBound(items, value);
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUpperBoundPos_manyItemsBegin() {
        List<Integer> items = list(5, 5, 5, 5, 5, 6, 6, 7, 8, 9);
        int value = 4;
        int expected = reliableGetUpperBound(items, value);
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUpperBoundPos_manyItemsEnd() {
        List<Integer> items = list(1, 2, 3, 4, 4, 5, 5, 5, 5, 5);
        int value = 6;
        int expected = reliableGetUpperBound(items, value);
        int result = MyListUtils.getUpperBoundPos(items, value);
        assertEquals(expected, result);
    }

    private static int reliableGetUpperBound(List<Integer> items, int value) {
        int pos = 0;
        for (int item : items) {
            if (item <= value) {
                ++pos;
            } else {
                break;
            }
        }
        return pos;
    }
}

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

package org.bitbucket.mlopatkin.android.logviewer.bookmarks;

import org.bitbucket.mlopatkin.android.logviewer.test.TestData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

public class BookmarkModelTest {

    private BookmarkModel model;

    @Mock
    private BookmarkModel.Observer observer;

    @Before
    public void setUp() {
        model = new BookmarkModel();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAsObservable() throws Exception {
        assertNotNull(model.asObservable());
    }

    @Test
    public void testContainsRecord() throws Exception {
        model.addRecord(TestData.RECORD1);

        assertTrue(model.containsRecord(TestData.RECORD1));
        assertFalse(model.containsRecord(TestData.RECORD2));
    }

    @Test
    public void testAddRecord() throws Exception {
        model.asObservable().addObserver(observer);

        model.addRecord(TestData.RECORD1);

        assertTrue(model.containsRecord(TestData.RECORD1));
        verify(observer).onBookmarkAdded();
    }

    @Test
    public void testRemoveRecord() throws Exception {
        model.addRecord(TestData.RECORD1);
        model.addRecord(TestData.RECORD2);

        model.asObservable().addObserver(observer);
        model.removeRecord(TestData.RECORD1);

        assertFalse(model.containsRecord(TestData.RECORD1));
        assertTrue(model.containsRecord(TestData.RECORD2));
        verify(observer).onBookmarkRemoved();
    }

    @Test
    public void testClear() throws Exception {
        model.addRecord(TestData.RECORD1);
        model.addRecord(TestData.RECORD2);
        model.asObservable().addObserver(observer);

        model.clear();

        verify(observer).onBookmarkRemoved();
        assertFalse(model.containsRecord(TestData.RECORD1));
        assertFalse(model.containsRecord(TestData.RECORD2));
    }
}

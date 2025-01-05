/*
 * Copyright 2020 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.ui.mainframe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.ui.filters.BufferFilterModel;

import org.junit.Test;

import java.util.EnumSet;

public class BufferFilterMenuTest {
    private final BufferFilterModel bufferFilter = mock();

    @Test
    public void menuIsHiddenIfNoBuffersAvailable() {
        BufferFilterMenu menu = new BufferFilterMenu(bufferFilter);
        menu.setAvailableBuffers(EnumSet.noneOf(LogRecord.Buffer.class));

        assertFalse(menu.getBuffersMenu().isVisible());
    }

    @Test
    public void menuIsShownIfABufferIsVisible() {
        BufferFilterMenu menu = new BufferFilterMenu(bufferFilter);
        menu.setAvailableBuffers(EnumSet.of(LogRecord.Buffer.MAIN));

        assertTrue(menu.getBuffersMenu().isVisible());
    }
}

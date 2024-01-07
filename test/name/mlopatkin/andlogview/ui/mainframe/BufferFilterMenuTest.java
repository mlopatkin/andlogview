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

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.ui.filters.LogModelFilterImpl;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.EnumSet;

import javax.swing.JMenu;

public class BufferFilterMenuTest {
    private final JMenu parentMenu = new JMenu();
    private final LogModelFilterImpl bufferFilter = Mockito.mock();

    @Test
    public void menuIsHiddenIfNoBuffersAvailable() {
        parentMenu.setVisible(true);

        BufferFilterMenu menu = new BufferFilterMenu(parentMenu, bufferFilter);
        menu.setAvailableBuffers(EnumSet.noneOf(LogRecord.Buffer.class));

        assertFalse(parentMenu.isVisible());
    }

    @Test
    public void menuIsShownIfABufferIsVisible() {
        parentMenu.setVisible(false);

        BufferFilterMenu menu = new BufferFilterMenu(parentMenu, bufferFilter);
        menu.setAvailableBuffers(EnumSet.of(LogRecord.Buffer.MAIN));

        assertTrue(parentMenu.isVisible());
    }
}

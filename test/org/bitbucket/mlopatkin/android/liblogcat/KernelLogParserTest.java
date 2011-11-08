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
package org.bitbucket.mlopatkin.android.liblogcat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class KernelLogParserTest {

    @Test
    public void testParseRecordSuccess() {
        String line = "<3>[ 4572.545557] Topology = 10be3";
        KernelLogRecord record = KernelLogParser.parseRecord(line);
        assertNotNull(record);
        assertEquals(line, record.getLine());
    }

    @Test
    public void testParseRecordOldStyle() {
        String line = "<3>Topology = 10be3";
        KernelLogRecord record = KernelLogParser.parseRecord(line);
        assertNotNull(record);
        assertEquals(line, record.getLine());
    }

    @Test
    public void testParseRecordFailed() {
        String line = "] ld9040 ea8868_gamma_ctl 3 3";
        KernelLogRecord record = KernelLogParser.parseRecord(line);
        assertNull(record);
    }
}

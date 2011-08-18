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
import static org.junit.Assert.assertSame;

import java.util.Calendar;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordParser;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Kind;
import org.junit.Test;

public class LogRecordParserTest {

    @Test
    public void testLogRecordParserThreadTime() {
        final String src = "07-29 16:15:29.787   296   721 D RPC: 3000008c:00050000 dispatching RPC call (XID 1866, xdr 0x3357b8) for callback client 3100008c:00050001.";
        Calendar calendar = Calendar.getInstance();
        calendar.set(1970, 6, 29, 16, 15, 29);
        calendar.set(Calendar.MILLISECOND, 787);
        final int pid = 296;
        final int tid = 721;
        final LogRecord.Priority priority = LogRecord.Priority.DEBUG;
        final String tag = "RPC";
        final String message = "3000008c:00050000 dispatching RPC call (XID 1866, xdr 0x3357b8) for callback client 3100008c:00050001.";

        LogRecord record = LogRecordParser.createThreadtimeRecord(Kind.UNKNOWN, LogRecordParser
                .parseLogRecordLine(src));

        assertEquals(calendar.getTime(), record.getTime());
        assertEquals(pid, record.getPid());
        assertEquals(tid, record.getTid());
        assertEquals(tag, record.getTag());
        assertSame(priority, record.getPriority());
        assertEquals(message, record.getMessage());
    }
}

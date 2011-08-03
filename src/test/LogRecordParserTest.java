package test;


import java.util.Calendar;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordParser;
import org.junit.Test;

import static org.junit.Assert.*;

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
        
        LogRecord record = LogRecordParser.parseThreadtimeRecord(src);
        
        assertEquals(calendar.getTime(), record.getTime());
        assertEquals(pid, record.getPid());
        assertEquals(tid, record.getTid());
        assertEquals(tag, record.getTag());
        assertSame(priority, record.getPriority());
        assertEquals(message, record.getMessage());
    }
}

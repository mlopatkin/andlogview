package name.mlopatkin.andlogview.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import name.mlopatkin.andlogview.liblogcat.LogRecord;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class RowSearchStrategyFactoryTest {
    static final String TAG_CONTACTS = "contacts";
    static final String TAG_SYSTEM = "AndroidRuntime";

    static final String APP_CONTACTS = "com.android.contacts";
    static final String APP_NAME_SYSTEM_SERVER = "system_server";

    static final String MSG_CONTACTS = "Starting activity com.android.contacts";
    static final String MSG_SYSTEM_SERVER = "System_server died";
    private LogRecord tagContacts;
    private LogRecord appnameContacts;
    private LogRecord msgContacts;
    private LogRecord system;

    private static LogRecord makeRecord(String tag, String app, String msg) {
        return new LogRecord(null, -1, -1, app, LogRecord.Priority.FATAL, tag, msg);
    }

    @Before
    public void setUp() throws Exception {
        tagContacts = makeRecord(TAG_CONTACTS, APP_NAME_SYSTEM_SERVER, MSG_SYSTEM_SERVER);
        appnameContacts = makeRecord(TAG_SYSTEM, APP_CONTACTS, MSG_SYSTEM_SERVER);
        msgContacts = makeRecord(TAG_SYSTEM, APP_NAME_SYSTEM_SERVER, MSG_CONTACTS);
        system = makeRecord(TAG_SYSTEM, APP_NAME_SYSTEM_SERVER, MSG_SYSTEM_SERVER);
    }

    @Test
    public void testCompile_Simple() throws Exception {
        RowSearchStrategy simple = RowSearchStrategyFactory.compile("contacts");

        assertNotNull(simple);
        assertTrue(simple.isRowMatched(msgContacts));
        assertTrue(simple.isRowMatched(appnameContacts));
        assertTrue(simple.isRowMatched(tagContacts));
        assertFalse(simple.isRowMatched(system));
    }

    @Test
    public void testCompile_Regex() throws Exception {
        RowSearchStrategy simple = RowSearchStrategyFactory.compile("/contacts/");

        assertNotNull(simple);
        assertTrue(simple.isRowMatched(msgContacts));
        assertTrue(simple.isRowMatched(appnameContacts));
        assertTrue(simple.isRowMatched(tagContacts));
        assertFalse(simple.isRowMatched(system));
    }

    @Test
    public void testCompile_ComplexRegex() throws Exception {
        RowSearchStrategy simple = RowSearchStrategyFactory.compile("/con.*ts/");

        assertNotNull(simple);
        assertTrue(simple.isRowMatched(msgContacts));
        assertTrue(simple.isRowMatched(appnameContacts));
        assertTrue(simple.isRowMatched(tagContacts));
        assertFalse(simple.isRowMatched(system));
    }

    @Test
    public void testCompile_AppNameSimple() throws Exception {
        RowSearchStrategy simple = RowSearchStrategyFactory.compile("app:contacts");

        assertNotNull(simple);
        assertFalse(simple.isRowMatched(msgContacts));
        assertTrue(simple.isRowMatched(appnameContacts));
        assertFalse(simple.isRowMatched(tagContacts));
        assertFalse(simple.isRowMatched(system));
    }

    @Test
    public void testCompile_TagSimple() throws Exception {
        RowSearchStrategy simple = RowSearchStrategyFactory.compile("tag:contacts");

        assertNotNull(simple);
        assertFalse(simple.isRowMatched(msgContacts));
        assertFalse(simple.isRowMatched(appnameContacts));
        assertTrue(simple.isRowMatched(tagContacts));
        assertFalse(simple.isRowMatched(system));
    }

    @Test
    public void testCompile_MsgSimple() throws Exception {
        RowSearchStrategy simple = RowSearchStrategyFactory.compile("msg:contacts");

        assertNotNull(simple);
        assertTrue(simple.isRowMatched(msgContacts));
        assertFalse(simple.isRowMatched(appnameContacts));
        assertFalse(simple.isRowMatched(tagContacts));
        assertFalse(simple.isRowMatched(system));
    }

    @Test
    public void testCompile_AppNameRegex() throws Exception {
        RowSearchStrategy simple = RowSearchStrategyFactory.compile("app:/c.ntacts/");

        assertNotNull(simple);
        assertFalse(simple.isRowMatched(msgContacts));
        assertTrue(simple.isRowMatched(appnameContacts));
        assertFalse(simple.isRowMatched(tagContacts));
        assertFalse(simple.isRowMatched(system));
    }

    @Test
    public void testCompile_TagRegex() throws Exception {
        RowSearchStrategy simple = RowSearchStrategyFactory.compile("tag:/c.ntacts/");

        assertNotNull(simple);
        assertFalse(simple.isRowMatched(msgContacts));
        assertFalse(simple.isRowMatched(appnameContacts));
        assertTrue(simple.isRowMatched(tagContacts));
        assertFalse(simple.isRowMatched(system));
    }

    @Test
    public void testCompile_MsgRegex() throws Exception {
        RowSearchStrategy simple = RowSearchStrategyFactory.compile("msg:/c.ntacts/");

        assertNotNull(simple);
        assertTrue(simple.isRowMatched(msgContacts));
        assertFalse(simple.isRowMatched(appnameContacts));
        assertFalse(simple.isRowMatched(tagContacts));
        assertFalse(simple.isRowMatched(system));
    }

    @Test
    @Ignore("Not supported yet")
    public void testCompile_Several() throws Exception {
        RowSearchStrategy simple = RowSearchStrategyFactory.compile("msg:/c.ntacts/   app:contacts");

        assertNotNull(simple);
        assertTrue(simple.isRowMatched(msgContacts));
        assertTrue(simple.isRowMatched(appnameContacts));
        assertFalse(simple.isRowMatched(tagContacts));
        assertFalse(simple.isRowMatched(system));
    }

    @Test
    @Ignore("No escaping, just treat this as a usual string")
    public void testCompile_Escape() throws Exception {
        LogRecord withEscaped = makeRecord(TAG_CONTACTS, APP_CONTACTS, "This contains a escaped app:contacts");

        RowSearchStrategy simple = RowSearchStrategyFactory.compile("app\\:contacts");

        assertNotNull(simple);
        assertFalse(simple.isRowMatched(msgContacts));
        assertFalse(simple.isRowMatched(appnameContacts));
        assertFalse(simple.isRowMatched(tagContacts));
        assertFalse(simple.isRowMatched(system));
        assertTrue(simple.isRowMatched(withEscaped));
    }

    @Test(expected = RequestCompilationException.class)
    @Ignore("No escaping, just treat this as a usual string")
    public void testCompile_InvalidPrefix() throws Exception {
        RowSearchStrategyFactory.compile("foobar:contacts");
    }

    @Test
    public void testCompile_Spaces() throws Exception {
        RowSearchStrategy simple = RowSearchStrategyFactory.compile("  \t    app:/c.ntacts/      ");

        assertNotNull(simple);
        assertFalse(simple.isRowMatched(msgContacts));
        assertTrue(simple.isRowMatched(appnameContacts));
        assertFalse(simple.isRowMatched(tagContacts));
        assertFalse(simple.isRowMatched(system));
    }

    @Test
    public void testCompile_Empty() throws Exception {
        assertNull(RowSearchStrategyFactory.compile(""));
        assertNull(RowSearchStrategyFactory.compile("          "));
        assertNull(RowSearchStrategyFactory.compile(null));
    }
}

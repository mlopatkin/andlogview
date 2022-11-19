/*
 * Copyright 2013 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.search.logrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.logmodel.LogRecordUtils;
import name.mlopatkin.andlogview.search.RequestCompilationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
        return LogRecordUtils.forMessage(msg).withTag(tag).withAppName(app);
    }

    @BeforeEach
    void setUp() throws Exception {
        tagContacts = makeRecord(TAG_CONTACTS, APP_NAME_SYSTEM_SERVER, MSG_SYSTEM_SERVER);
        appnameContacts = makeRecord(TAG_SYSTEM, APP_CONTACTS, MSG_SYSTEM_SERVER);
        msgContacts = makeRecord(TAG_SYSTEM, APP_NAME_SYSTEM_SERVER, MSG_CONTACTS);
        system = makeRecord(TAG_SYSTEM, APP_NAME_SYSTEM_SERVER, MSG_SYSTEM_SERVER);
    }

    @Test
    public void simplePatternMatchesAllFields() throws Exception {
        var strategy = RowSearchStrategyFactory.compile("contacts");

        assertThat(strategy)
                .accepts(msgContacts, appnameContacts, tagContacts)
                .rejects(system);
    }

    @Test
    public void plainRegexPatternMatchesAllFields() throws Exception {
        var strategy = RowSearchStrategyFactory.compile("/contacts/");

        assertThat(strategy)
                .accepts(msgContacts, appnameContacts, tagContacts)
                .rejects(system);

    }

    @Test
    public void trickyRegex() throws Exception {
        var strategy = RowSearchStrategyFactory.compile("/con.*ts/");

        assertThat(strategy)
                .accepts(msgContacts, appnameContacts, tagContacts)
                .rejects(system);
    }

    @Test
    public void testCompile_AppNameSimple() throws Exception {
        var strategy = RowSearchStrategyFactory.compile("app:contacts");

        assertThat(strategy)
                .accepts(appnameContacts)
                .rejects(msgContacts, tagContacts, system);
    }

    @Test
    public void testCompile_TagSimple() throws Exception {
        var strategy = RowSearchStrategyFactory.compile("tag:contacts");

        assertThat(strategy)
                .accepts(tagContacts)
                .rejects(msgContacts, appnameContacts, system);
    }

    @Test
    public void testCompile_MsgSimple() throws Exception {
        var strategy = RowSearchStrategyFactory.compile("msg:contacts");

        assertThat(strategy)
                .accepts(msgContacts)
                .rejects(appnameContacts, tagContacts, system);
    }

    @Test
    public void testCompile_AppNameRegex() throws Exception {
        var strategy = RowSearchStrategyFactory.compile("app:/c.ntacts/");

        assertThat(strategy)
                .accepts(appnameContacts)
                .rejects(msgContacts, tagContacts, system);
    }

    @Test
    public void testCompile_TagRegex() throws Exception {
        var strategy = RowSearchStrategyFactory.compile("tag:/c.ntacts/");

        assertThat(strategy)
                .accepts(tagContacts)
                .rejects(msgContacts, appnameContacts, system);
    }

    @Test
    public void testCompile_MsgRegex() throws Exception {
        var strategy = RowSearchStrategyFactory.compile("msg:/c.ntacts/");

        assertThat(strategy)
                .accepts(msgContacts)
                .rejects(appnameContacts, tagContacts, system);
    }

    @Test
    @Disabled("No escaping, just treat this as a usual string")
    public void testCompile_Escape() throws Exception {
        LogRecord withEscaped = makeRecord(TAG_CONTACTS, APP_CONTACTS, "This contains a escaped app:contacts");

        var strategy = RowSearchStrategyFactory.compile("app\\:contacts");

        assertThat(strategy)
                .accepts(withEscaped)
                .rejects(appnameContacts, msgContacts, tagContacts, system);
    }

    @Test
    public void testCompile_InvalidPrefix() throws Exception {
        var strategy = RowSearchStrategyFactory.compile("foobar:contacts");

        assertThat(strategy)
                .accepts(LogRecordUtils.forMessage("Some foobar:contacts"))
                .rejects(LogRecordUtils.forMessage("Some contacts"));
    }

    @Test
    public void testCompile_Spaces() throws Exception {
        var strategy = RowSearchStrategyFactory.compile("  \t    app:/c.ntacts/      ");

        assertThat(strategy)
                .accepts(appnameContacts)
                .rejects(msgContacts, tagContacts, system);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "            ",
        "        app:  ",
        "\t\t\t\n",
        "app:",
        " "
    })
    public void testCompile_Empty(String invalidPattern) throws Exception {
        assertThatExceptionOfType(RequestCompilationException.class)
                .isThrownBy(() -> RowSearchStrategyFactory.compile(invalidPattern));
    }
}

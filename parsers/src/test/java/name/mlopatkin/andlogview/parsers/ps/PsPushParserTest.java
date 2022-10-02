/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.parsers.ps;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.thirdparty.device.AndroidVersionCodes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;

import java.util.Arrays;
import java.util.Collection;

class PsPushParserTest {
    private static final String[] PS_SIMPLE = {
            "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
            "root      1     0     244    228   fg  ffffffff 00000000 S /init"
    };
    private static final String[] PS_TWO_WORDS_PROCESS_NAME = {
            "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
            "root      279   2     0      0     fg  ffffffff 00000000 S Two words"
    };
    private static final String[] PS_NO_PCY = {
            "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
            "root      12    2     0      0         ffffffff 00000000 S sync_supers"
    };
    private static final String[] PS_NO_NAME = {
            "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
            "root      626   2     0      0     fg  ffffffff 00000000 S ",
            };
    private static final String[] PS_WCHAN_SYMBOL = {
            "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
            "root      29392 2     0      0     fg  cpu_stoppe 00000000 S migration/3"
    };

    private static final String[] PS_NO_WCHAN = {
            "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
            "u0_a251   4851  216   884420 35064 bg             00000000 R com.mapswithme.maps.pro"
    };
    private static final String[] PS_IDLE_KERNEL_THREAD = {
            "USER           PID  PPID     VSZ    RSS WCHAN            ADDR S NAME                       ",
            "root            53     2       0      0 0                   0 I [kworker/6:0H-events_highpri]"
    };

    @Test
    public void testParseProcessListLine() {
        PsParseEventsHandler eventsHandler = createMockHandler();

        parseLines(eventsHandler, PS_SIMPLE);

        verify(eventsHandler).processLine(1, "/init");
    }

    @Test
    public void testParseProcessNameWithSpaces() {
        PsParseEventsHandler eventsHandler = createMockHandler();

        parseLines(eventsHandler, PS_TWO_WORDS_PROCESS_NAME);

        verify(eventsHandler).processLine(279, "Two words");
    }

    @Test
    public void testParseProcessListNoPCY() {
        PsParseEventsHandler eventsHandler = createMockHandler();

        parseLines(eventsHandler, PS_NO_PCY);

        verify(eventsHandler).processLine(12, "sync_supers");
    }

    @Test
    public void testParseProcessListNoName() {
        PsParseEventsHandler eventsHandler = createMockHandler();

        parseLines(eventsHandler, PS_NO_NAME);

        verify(eventsHandler).processLine(626, "");
    }

    @Test
    public void testParseProcessListSymbolicWchan() throws Exception {
        PsParseEventsHandler eventsHandler = createMockHandler();

        parseLines(eventsHandler, PS_WCHAN_SYMBOL);

        verify(eventsHandler).processLine(29392, "migration/3");
    }

    @Test
    public void testParseProcessListMissingWchan() throws Exception {
        PsParseEventsHandler eventsHandler = createMockHandler();

        parseLines(eventsHandler, PS_NO_WCHAN);

        verify(eventsHandler).processLine(4851, "com.mapswithme.maps.pro");
    }

    @Test
    public void testParseProcessListIdleKernel() throws Exception {
        PsParseEventsHandler eventsHandler = createMockHandler();

        parseLines(eventsHandler, PS_IDLE_KERNEL_THREAD);

        verify(eventsHandler).processLine(53, "[kworker/6:0H-events_highpri]");
    }

    @ParameterizedTest(name = "sdkVersion = {0}")
    @MethodSource
    void compatibilityWithAdbPsData(int ignoredSdkVersion, String header, String psLine) {
        PsParseEventsHandler eventsHandler = createMockHandler();

        try (PsPushParser pushParser = new PsPushParser(eventsHandler)) {
            assertTrue(pushParser.nextLine(header), "Parser stopped after header");
            assertTrue(pushParser.nextLine(psLine), "Parser stopped after first line");
        }

        InOrder order = inOrder(eventsHandler);
        order.verify(eventsHandler).header();
        order.verify(eventsHandler).processLine(1998, "com.android.phone");
        order.verify(eventsHandler).documentEnded();
        order.verifyNoMoreInteractions();
    }

    private static PsParseEventsHandler createMockHandler() {
        PsParseEventsHandler eventsHandler =
                mock(PsParseEventsHandler.class, invocation -> ParserControl.proceed());
        doAnswer(invocation -> null).when(eventsHandler).documentEnded();
        return eventsHandler;
    }

    static Collection<Object[]> compatibilityWithAdbPsData() {
        return Arrays.asList(new Object[][] {
                {
                        AndroidVersionCodes.GINGERBREAD_MR1,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC         NAME",
                        "radio     1998  1162  191340 26512 ffffffff 8011466b S com.android.phone",
                        },
                {
                        AndroidVersionCodes.ICE_CREAM_SANDWICH_MR1,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC         NAME",
                        "radio     1998  1164  307580 32356 ffffffff b770ded7 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.JELLY_BEAN,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC         NAME",
                        "radio     1998  1165  327664 31596 ffffffff b7712157 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.JELLY_BEAN_MR1,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC         NAME",
                        "radio     1998  1165  337944 26588 ffffffff b7671827 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.JELLY_BEAN_MR2,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC         NAME",
                        "radio     1998  1167  353212 29068 ffffffff b773c89b S com.android.phone",
                        },
                {
                        AndroidVersionCodes.KITKAT,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC         NAME",
                        "radio     1998  1175  379100 30900 ffffffff b7749f1b S com.android.phone",
                        },
                {
                        AndroidVersionCodes.LOLLIPOP,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC        NAME",
                        "radio     1998  1187  721476 48248 ffffffff b7677f15 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.LOLLIPOP_MR1,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC        NAME",
                        "radio     1998  1189  858364 45288 ffffffff b756b5b5 S com.android.phone",
                        },
                {
                        23 /* M */,
                        "USER      PID   PPID  VSIZE  RSS   WCHAN            PC  NAME",
                        "radio     1998  1308  889828 50184 SyS_epoll_ b7363fa5 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.N,
                        "USER      PID   PPID  VSIZE  RSS   WCHAN            PC  NAME",
                        "radio     1998  1448  1020332 63872 SyS_epoll_ 00000000 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.N_MR1,
                        "USER      PID   PPID  VSIZE  RSS   WCHAN            PC  NAME",
                        "radio     1998  1450  1007260 52480 SyS_epoll_ 00000000 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.O,
                        "USER           PID  PPID     VSZ    RSS WCHAN            ADDR S NAME                       ",
                        "radio         1998  1890 1023936  64040 SyS_epoll_wait      0 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.O_MR1,
                        "USER           PID  PPID     VSZ    RSS WCHAN            ADDR S NAME                       ",
                        "radio         1998  1578  999616  49816 ep_poll             0 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.P,
                        "USER           PID  PPID     VSZ    RSS WCHAN            ADDR S NAME                       ",
                        "radio         1998  1754 1076492 121280 0                   0 S com.android.phone",
                        },

                });
    }

    @ParameterizedTest(name = "sdkVersion = {0}")
    @MethodSource
    void compatibilityWithDumpstatePsData(int ignoredSdkVersion, String header, String psLine) {
        PsParseEventsHandler eventsHandler = createMockHandler();

        try (PsPushParser pushParser = new PsPushParser(eventsHandler)) {
            assertTrue(pushParser.nextLine(header), "Parser stopped after header");
            assertTrue(pushParser.nextLine(psLine), "Parser stopped after first line");
        }

        InOrder order = inOrder(eventsHandler);
        order.verify(eventsHandler).header();
        order.verify(eventsHandler).processLine(1998, "com.android.phone");
        order.verify(eventsHandler).documentEnded();
        order.verifyNoMoreInteractions();
    }

    static Collection<Object[]> compatibilityWithDumpstatePsData() {
        return Arrays.asList(new Object[][] {
                {
                        AndroidVersionCodes.GINGERBREAD_MR1,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
                        "radio     1998  1162  191340 26456 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.ICE_CREAM_SANDWICH_MR1,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
                        "radio     1998  1164  307580 32356 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.JELLY_BEAN,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
                        "radio     1998  1165  327664 31556 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.JELLY_BEAN_MR1,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
                        "radio     1998  1165  337948 26564 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.JELLY_BEAN_MR2,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
                        "radio     1998  1167  353120 28728 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.KITKAT,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
                        "radio     1998  1175  379056 30432 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.LOLLIPOP,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC        NAME",
                        "radio     1998  1187  721316 46300 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.LOLLIPOP_MR1,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC        NAME",
                        "radio     1998  1189  857152 44356 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        AndroidVersionCodes.M,
                        "USER      PID   PPID  VSIZE  RSS   PCY WCHAN            PC  NAME",
                        "radio     1998  1308  889392 49260 fg  SyS_epoll_ 00000000 S com.android.phone",
                        },
                });
    }

    private static void parseLines(PsParseEventsHandler handler, String[] lines) {
        try (PsPushParser pushParser = new PsPushParser(handler)) {
            for (String line : lines) {
                assertTrue(pushParser.nextLine(line), "Parser should accept all lines");
            }
        }
    }
}

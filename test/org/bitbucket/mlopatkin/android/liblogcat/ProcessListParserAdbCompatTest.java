/*
 * Copyright 2018 Mikhail Lopatkin
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

import com.android.sdklib.AndroidVersion;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.bitbucket.mlopatkin.android.liblogcat.ProcessListParserTest.hasPidAndAppName;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Compatibility tests for parsing output of the plain "ps" command.
 */
@RunWith(Parameterized.class)
public class ProcessListParserAdbCompatTest {
    private final String header;
    private final String psLine;

    @Parameterized.Parameters(name = "sdkVersion = {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {
                        AndroidVersion.VersionCodes.GINGERBREAD_MR1,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC         NAME",
                        "radio     1998  1162  191340 26512 ffffffff 8011466b S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.ICE_CREAM_SANDWICH_MR1,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC         NAME",
                        "radio     1998  1164  307580 32356 ffffffff b770ded7 S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.JELLY_BEAN,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC         NAME",
                        "radio     1998  1165  327664 31596 ffffffff b7712157 S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.JELLY_BEAN_MR1,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC         NAME",
                        "radio     1998  1165  337944 26588 ffffffff b7671827 S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.JELLY_BEAN_MR2,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC         NAME",
                        "radio     1998  1167  353212 29068 ffffffff b773c89b S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.KITKAT,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC         NAME",
                        "radio     1998  1175  379100 30900 ffffffff b7749f1b S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.LOLLIPOP,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC        NAME",
                        "radio     1998  1187  721476 48248 ffffffff b7677f15 S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.LOLLIPOP_MR1,
                        "USER     PID   PPID  VSIZE  RSS     WCHAN    PC        NAME",
                        "radio     1998  1189  858364 45288 ffffffff b756b5b5 S com.android.phone",
                        },
                {
                        23 /* M */,
                        "USER      PID   PPID  VSIZE  RSS   WCHAN            PC  NAME",
                        "radio     1998  1308  889828 50184 SyS_epoll_ b7363fa5 S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.N,
                        "USER      PID   PPID  VSIZE  RSS   WCHAN            PC  NAME",
                        "radio     1998  1448  1020332 63872 SyS_epoll_ 00000000 S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.N_MR1,
                        "USER      PID   PPID  VSIZE  RSS   WCHAN            PC  NAME",
                        "radio     1998  1450  1007260 52480 SyS_epoll_ 00000000 S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.O,
                        "USER           PID  PPID     VSZ    RSS WCHAN            ADDR S NAME                       ",
                        "radio         1998  1890 1023936  64040 SyS_epoll_wait      0 S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.O_MR1,
                        "USER           PID  PPID     VSZ    RSS WCHAN            ADDR S NAME                       ",
                        "radio         1998  1578  999616  49816 ep_poll             0 S com.android.phone",
                        },
                {
                        28 /* P */,
                        "USER           PID  PPID     VSZ    RSS WCHAN            ADDR S NAME                       ",
                        "radio         1998  1754 1076492 121280 0                   0 S com.android.phone",
                        },

                });
    }

    @SuppressWarnings("unused")
    public ProcessListParserAdbCompatTest(int sdkVersion, String header, String psLine) {
        this.header = header;
        this.psLine = psLine;
    }

    @Test
    public void compatibilityWithPsData() {
        assertTrue(ProcessListParser.isProcessListHeader(header));
        assertThat(ProcessListParser.parseProcessListLine(psLine), hasPidAndAppName(1998, "com.android.phone"));
    }
}

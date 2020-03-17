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

@RunWith(Parameterized.class)
public class ProcessListParserDumpstateCompatTest {
    private final String header;
    private final String psLine;

    @Parameterized.Parameters(name = "sdkVersion = {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {
                        AndroidVersion.VersionCodes.GINGERBREAD_MR1,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
                        "radio     1998  1162  191340 26456 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.ICE_CREAM_SANDWICH_MR1,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
                        "radio     1998  1164  307580 32356 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.JELLY_BEAN,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
                        "radio     1998  1165  327664 31556 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.JELLY_BEAN_MR1,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
                        "radio     1998  1165  337948 26564 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.JELLY_BEAN_MR2,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
                        "radio     1998  1167  353120 28728 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.KITKAT,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC         NAME",
                        "radio     1998  1175  379056 30432 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.LOLLIPOP,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC        NAME",
                        "radio     1998  1187  721316 46300 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        AndroidVersion.VersionCodes.LOLLIPOP_MR1,
                        "USER     PID   PPID  VSIZE  RSS    PCY  WCHAN    PC        NAME",
                        "radio     1998  1189  857152 44356 fg  ffffffff 00000000 S com.android.phone",
                        },
                {
                        23,
                        "USER      PID   PPID  VSIZE  RSS   PCY WCHAN            PC  NAME",
                        "radio     1998  1308  889392 49260 fg  SyS_epoll_ 00000000 S com.android.phone",
                        },
                });
    }

    @SuppressWarnings("unused")
    public ProcessListParserDumpstateCompatTest(int sdkVersion, String header, String psLine) {
        this.header = header;
        this.psLine = psLine;
    }

    @Test
    public void compatibilityWithDumpstateData() {
        assertTrue(ProcessListParser.isProcessListHeader(header));
        assertThat(ProcessListParser.parseProcessListLine(psLine), hasPidAndAppName(1998, "com.android.phone"));
    }
}

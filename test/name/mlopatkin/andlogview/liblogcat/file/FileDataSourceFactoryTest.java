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

package name.mlopatkin.andlogview.liblogcat.file;

import static org.hamcrest.MatcherAssert.assertThat;

import name.mlopatkin.andlogview.logmodel.DataSource;
import name.mlopatkin.andlogview.logmodel.Field;
import name.mlopatkin.andlogview.logmodel.LogRecord.Buffer;

import com.google.common.io.CharSource;
import com.google.common.io.Resources;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class FileDataSourceFactoryTest {
    @Test(expected = UnrecognizedFormatException.class)
    public void openEmptyFile() throws Exception {
        FileDataSourceFactory.createDataSource("empty.log", CharSource.empty());
    }

    @Test(expected = UnrecognizedFormatException.class)
    public void openBlankFile() throws Exception {
        FileDataSourceFactory.createDataSource("blank.log", CharSource.wrap("    \n\n   \n\t\t"));
    }

    @Test
    public void openDumpstate() throws Exception {
        CharSource dumpstateFile = openTestData("galaxy_nexus_jbmr2.minimized.dump");

        DataSource dumpstate = FileDataSourceFactory.createDataSource("dumpstate.log", dumpstateFile);
        assertThat(dumpstate.getAvailableBuffers(),
                Matchers.containsInAnyOrder(Buffer.EVENTS, Buffer.RADIO, Buffer.MAIN));
    }

    @Test
    public void openBrief() throws Exception {
        CharSource briefLog = openTestData("galaxy_nexus_jbmr2_brief.log");

        DataSource brief = FileDataSourceFactory.createDataSource("brief.log", briefLog);
        assertThat(brief.getAvailableFields(),
                Matchers.containsInAnyOrder(Field.PRIORITY, Field.TAG, Field.PID, Field.MESSAGE));
    }

    @Test
    public void openTime() throws Exception {
        CharSource log = openTestData("galaxy_nexus_jbmr2_time.log");

        DataSource source = FileDataSourceFactory.createDataSource("time.log", log);
        assertThat(source.getAvailableFields(),
                Matchers.containsInAnyOrder(Field.TIME, Field.PRIORITY, Field.TAG, Field.PID, Field.MESSAGE));
    }

    @Test
    public void openThreadtime() throws Exception {
        CharSource log = openTestData("galaxy_nexus_jbmr2_threadtime.log");

        DataSource source = FileDataSourceFactory.createDataSource("threadtime.log", log);
        assertThat(source.getAvailableFields(),
                Matchers.containsInAnyOrder(
                        Field.TIME, Field.PID, Field.TID, Field.PRIORITY, Field.TAG, Field.MESSAGE));
    }

    @Test
    public void openLogFileWithExtraStuffInTheBeginning() throws Exception {
        CharSource extraStuffLog = openTestData("huawei_p10_log_snippet.log");

        DataSource source = FileDataSourceFactory.createDataSource("huawei.log", extraStuffLog);
        source.close();
    }

    @Test
    public void openAndroidStudioLog() throws Exception {
        CharSource log = openTestData("emulator_cupcake_android_studio.log");

        DataSource source = FileDataSourceFactory.createDataSource("androidstudio.log", log);
        assertThat(source.getAvailableFields(),
                Matchers.containsInAnyOrder(
                        Field.TIME, Field.PID, Field.TID, Field.PRIORITY, Field.TAG, Field.MESSAGE, Field.APP_NAME));
    }

    private CharSource openTestData(String testDataName) {
        return Resources.asCharSource(Resources.getResource(getClass(), testDataName), StandardCharsets.UTF_8);
    }
}

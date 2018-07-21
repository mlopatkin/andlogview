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

package org.bitbucket.mlopatkin.android.liblogcat.file;

import com.google.common.io.CharSource;

import org.junit.Test;

public class FileDataSourceFactoryTest {

    @Test(expected = UnrecognizedFormatException.class)
    public void openEmptyFile() throws Exception {
        FileDataSourceFactory.createDataSource("empty.log", CharSource.empty());
    }

    @Test(expected = UnrecognizedFormatException.class)
    public void openBlankFile() throws Exception {
        FileDataSourceFactory.createDataSource("blank.log", CharSource.wrap("    \n\n   \n\t\t"));
    }
}

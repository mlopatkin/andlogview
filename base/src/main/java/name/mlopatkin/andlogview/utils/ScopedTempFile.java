/*
 * Copyright 2021 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.utils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ScopedTempFile implements Closeable {
    private final File file;

    public ScopedTempFile(String prefix, String suffix) throws IOException {
        file = File.createTempFile(prefix, suffix);
    }

    public File getFile() {
        return file;
    }

    @Override
    public void close() throws IOException {
        Files.delete(file.toPath());
    }
}

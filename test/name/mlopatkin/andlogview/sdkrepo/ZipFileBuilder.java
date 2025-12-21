/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview.sdkrepo;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.assertj.core.util.CanIgnoreReturnValue;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ZipFileBuilder implements Closeable {
    private final ZipArchiveOutputStream outputStream;

    public ZipFileBuilder(Path targetZipPath) throws IOException {
        outputStream = new ZipArchiveOutputStream(targetZipPath.toFile());
    }

    @CanIgnoreReturnValue
    ZipFileBuilder file(String entryPath, String content) throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry(entryPath);
        outputStream.putArchiveEntry(entry);
        try (var entryWriter = new BufferedWriter(
                new OutputStreamWriter(
                        CloseShieldOutputStream.wrap(outputStream),
                        StandardCharsets.UTF_8
                )
        )) {
            entryWriter.append(content);
        }
        outputStream.closeArchiveEntry();

        return this;
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }


    public static Path zipFile(Path targetZipPath, IOConsumer<? super ZipFileBuilder> consumer) throws IOException {
        try (var zipFile = new ZipFileBuilder(targetZipPath)) {
            consumer.accept(zipFile);
        }

        return targetZipPath;
    }
}

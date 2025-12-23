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

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * Helper stream wrapper that adds extra context to exceptions thrown by the underlying output stream.
 */
class ExceptionTranslatingOutputStream extends FilterOutputStream {
    private final File outputFile;

    public ExceptionTranslatingOutputStream(File outputFile, OutputStream out) {
        super(out);
        this.outputFile = outputFile;
    }

    private SdkException error(IOException cause) throws SdkException {
        throw SdkException.rethrow(cause, "Failed to write into `%s`", outputFile.getAbsolutePath());
    }

    @Override
    public void write(int b) throws SdkException {
        try {
            super.write(b);
        } catch (IOException e) {
            throw error(e);
        }
    }

    @Override
    public void write(byte[] b) throws SdkException {
        try {
            super.write(b);
        } catch (IOException e) {
            throw error(e);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws SdkException {
        try {
            super.write(b, off, len);
        } catch (IOException e) {
            throw error(e);
        }
    }

    @Override
    public void flush() throws SdkException {
        try {
            super.flush();
        } catch (IOException e) {
            throw error(e);
        }
    }

    @Override
    public void close() throws SdkException {
        try {
            super.close();
        } catch (IOException e) {
            throw error(e);
        }
    }

    public static OutputStream open(File outputFile) throws SdkException {
        try {
            return new ExceptionTranslatingOutputStream(outputFile, Files.newOutputStream(outputFile.toPath()));
        } catch (IOException e) {
            throw SdkException.rethrow(e, "Failed to open file `%s` for writing.", outputFile);
        }
    }
}

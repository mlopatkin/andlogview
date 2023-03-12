/*
 * Copyright 2023 the Andlogview authors
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

package name.mlopatkin.andlogview.base.io;

import com.google.common.base.Preconditions;
import com.google.common.io.CharSource;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

/**
 * This class functions similarly to {@link BufferedReader}, but only provides the ability to read whole lines.
 * In addition, it includes line-terminating characters in the read lines, similar to Python's {@code lines} method.
 */
public class LineReader implements Closeable {
    // Beware, this class is optimized for performance. All changes to it should be verified with LineReaderPerfTest.
    // Currently, it beats BufferedReader by about 5% in "\n" case.

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int EXPECTED_LINE_LENGTH = 150;

    private final Reader input;

    private final char[] buffer;
    private int bufStart = 0;
    private int bufEnd = 0;

    private boolean shouldConsumeNextLf;

    /**
     * Creates the new line reader that uses {@code input} as the source of char data.
     *
     * @param source the source of the data
     * @param bufferSize the size of the internal buffer
     * @throws IOException if opening the stream out of the source fails
     */
    public LineReader(CharSource source, int bufferSize) throws IOException {
        this(source.openStream(), bufferSize);
    }

    /**
     * Creates the new line reader that uses {@code input} as the source of char data and with the default buffer size.
     *
     * @param source the source of the data
     * @throws IOException if opening the stream out of the source fails
     */
    public LineReader(CharSource source) throws IOException {
        this(source, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates the new line reader that uses {@code input} as the source of char data and the default buffer size. The
     * line reader takes ownership of the provided reader and closes it upon calling {@link #close()}. If something else
     * reads from the provided input, the behavior is undefined.
     *
     * @param input the reader to provide data
     */
    public LineReader(Reader input) {
        this(input, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates the new line reader that uses {@code input} as the source of char data. The line reader takes ownership
     * of the provided reader and closes it upon calling {@link #close()}. If something else reads from the provided
     * input, the behavior is undefined.
     *
     * @param input the reader to provide data
     * @param bufferSize the size of the internal buffer
     */
    public LineReader(Reader input, int bufferSize) {
        Preconditions.checkArgument(bufferSize > 0, "Buffer size %s is too small, must be positive", bufferSize);

        this.input = input;
        this.buffer = new char[bufferSize];

    }

    /**
     * Returns the next line of the input, or {@code null} if there is no more data in it. Unlike
     * {@link BufferedReader#readLine()}, this method includes line-terminating character in the result.
     * Line endings are normalized, so only {@code \n} is used, even if the source reader has {@code \r} or
     * {@code \r\n}.
     *
     * @return the next line of the input or {@code null} if the input is exhausted.
     * @throws IOException if reading the input fails
     */
    @Nullable
    @SuppressWarnings("NullAway")
    // NullAway cannot figure out that (result != null || resultBuffer != null) is the do-while cycle's post-condition.
    // It can be probably fixed with assert, but I don't want to add new bytecodes to this optimized method.
    public String readLine() throws IOException {
        consumeLfTailIfNeeded();
        if (!ensureBuffer()) {
            return null;
        }

        String result = null;
        StringBuilder resultBuffer = null;

        do {
            int eolnPos = eolnPosInBuffer();
            if (eolnPos >= 0) {
                if (buffer[eolnPos] == '\r') {
                    shouldConsumeNextLf = true;
                    // Convert the EOLN, so we can copy it into result.
                    buffer[eolnPos] = '\n';
                }
                int start = bufStart;
                int len = eolnPos - start + 1;
                // Copy the buffer until EOLN, inclusive.
                if (resultBuffer == null) {
                    result = new String(buffer, start, len);
                } else {
                    result = resultBuffer.append(buffer, start, len).toString();
                }
                // Don't move this up, for whatever reason JIT loves having this update at the end.
                bufStart = eolnPos + 1;
                break;
            } else {
                int len = bufEnd - bufStart;
                if (resultBuffer == null) {
                    resultBuffer = new StringBuilder(EXPECTED_LINE_LENGTH);
                }
                // No EOLN in the buffer, append the whole buffer, fill it and start again.
                resultBuffer.append(buffer, bufStart, len);
                // the buffer is now empty.
                bufStart = bufEnd;
            }
        } while (fillBuffer());
        return result != null ? result : resultBuffer.toString();
    }

    /**
     * Looks up the position of the leftmost end-of-line (EOLN) character in the buffer. The EOLN chars are {@code \n}
     * or {@code \r}. This method doesn't touch the input source.
     *
     * @return the position of the EOLN character in the buffer or -1 if there is no such character.
     */
    private int eolnPosInBuffer() {
        for (int pos = bufStart, last = bufEnd; pos < last; ++pos) {
            char c = buffer[pos];
            if (c == '\r' || c == '\n') {
                return pos;
            }
        }
        return -1;
    }

    /**
     * Checks if the previous line ended in {@code \r}, so there's a potential trailing {@code \n} remaining, then
     * consumes the trailing {@code \n}. May fill the buffer is it empty.
     *
     * @throws IOException if filling the buffer failed
     */
    private void consumeLfTailIfNeeded() throws IOException {
        if (shouldConsumeNextLf && ensureBuffer()) {
            if (buffer[bufStart] == '\n') {
                // Discard the '\n' suffix of a previously read '\r\n'
                ++bufStart;
            }
            shouldConsumeNextLf = false;
        }
    }

    /**
     * Ensures that the buffer is not empty, filling it from the input if necessary. Calling this method when the buffer
     * is not empty has no effect.
     *
     * @return {@code true} if the buffer is not empty, {@code false} if the buffer cannot be filled because the input
     *         is also empty
     * @throws IOException if reading from the input fails
     */
    private boolean ensureBuffer() throws IOException {
        return hasSomeInBuffer() || fillBuffer();
    }

    /**
     * Checks if the buffer is not empty.
     *
     * @return {@code true} if the buffer is not empty
     */
    private boolean hasSomeInBuffer() {
        return bufStart < bufEnd;
    }

    /**
     * Fills the buffer from the input, discarding existing buffer contents.
     *
     * @return {@code true} if the buffer was filled, {@code false} if the input is empty
     * @throws IOException if reading from the input fails
     */
    private boolean fillBuffer() throws IOException {
        bufStart = 0;
        bufEnd = input.read(buffer);
        return bufEnd >= 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Closes the reader provided as an argument too.
     */
    @Override
    public void close() throws IOException {
        input.close();
    }
}

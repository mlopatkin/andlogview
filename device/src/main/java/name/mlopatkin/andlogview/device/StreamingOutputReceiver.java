/*
 * Copyright 2020 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.device;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * {@link IShellOutputReceiver} that forwards all received data into the underlying {@link OutputStream}. Processing
 * stops if the underlying stream throws {@link IOException} during writes. Such situation can be detected with {@link
 * #hasException()} method and pending exception can be retrieved. The instance of the class should not be reused if the
 * exception happens.
 * <p>
 * Note on threading: it is safe to query exception status on any thread. The underlying stream is accessed on the
 * thread that performs {@link IDevice#executeShellCommand(String, IShellOutputReceiver)}.
 */
class StreamingOutputReceiver implements IShellOutputReceiver {
    private final OutputStream output;
    private volatile @MonotonicNonNull IOException pendingException;

    /**
     * Creates a new receiver. The receiver doesn't close the output stream. Closing of the stream is responsibility of
     * the caller.
     *
     * @param output the stream to write command output into
     */
    public StreamingOutputReceiver(OutputStream output) {
        this.output = output;
    }

    @Override
    public void addOutput(byte[] data, int offset, int length) {
        if (pendingException != null) {
            return;
        }
        try {
            output.write(data, offset, length);
        } catch (IOException e) {
            pendingException = e;
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public boolean isCancelled() {
        return pendingException != null;
    }

    /**
     * @return {@code true} if there is an {@link IOException} from the underlying stream pending
     */
    public boolean hasException() {
        return pendingException != null;
    }

    /**
     * Returns pending exception if {@link #hasException()} returns {@code true}. Otherwise, throws
     * {@code NullPointerException}.
     *
     * @return the pending {@link IOException}
     * @throws NullPointerException if there is no pending exception
     */
    public IOException getException() {
        return Objects.requireNonNull(pendingException);
    }
}

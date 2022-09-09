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

package name.mlopatkin.andlogview.device;

import com.android.ddmlib.IDevice;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents the destination of the output redirection.
 */
public abstract class OutputTarget {
    private OutputTarget() {}

    // The sophisticated class hierarchy there is to support ForStdout- and ForStderr- only subtypes.

    /**
     * This is the type of destination suitable for redirecting {@code stdout} and {@code stderr} streams.
     */
    public abstract static class ForStdoutAndStderr extends ForStdout {
        private ForStdoutAndStderr() {}
    }

    /**
     * This is the type of destination suitable for redirecting {@code stdout} stream.
     */
    public abstract static class ForStdout extends ForStderr {
        private ForStdout() {}
    }

    /**
     * This is the type of destination suitable for redirecting {@code stderr} stream.
     */
    public abstract static class ForStderr extends OutputTarget {
        private ForStderr() {}
    }

    /**
     * Redirects the stream into {@code /dev/null}.
     *
     * @return the redirector
     */
    public static ForStdoutAndStderr toDevNull() {
        return NullOutputTarget.INSTANCE;
    }

    /**
     * Redirects the stream into the output stream. The provided stream is not closed by the command.
     *
     * @param stream the stream to write contents of the standard stream into
     * @return the redirector
     */
    public static ForStdoutAndStderr toOutputStream(OutputStream stream) {
        return new StreamOutputTarget(stream);
    }

    /**
     * Doesn't redirect the stream at all. Use sparingly as the {@link Command#execute()} uses stream redirection
     * internally and disabling it may break exit code analysis.
     *
     * @return the redirector
     */
    static ForStdoutAndStderr noRedirection() {
        return NoRedirTarget.INSTANCE;
    }

    /**
     * Redirects the {@code stderr} stream into {@code stdout}. The latter can be redirected somewhere else.
     *
     * @return the redirector
     */
    public static ForStderr toStdout() {
        return StdoutRedirOutputTarget.INSTANCE;
    }

    abstract OutputHandle openOutput(IDevice device);

    enum StdStream {
        STDOUT("1"),
        STDERR("2");

        private final String id;

        StdStream(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    interface OutputHandle extends AutoCloseable {
        /**
         * Returns the full "sh"-compatible redirect string, e.g. {@code "1>/dev/null"}.
         *
         * @param redirectFrom the stream to redirect from
         * @return the redirect string
         */
        String getRedirectString(StdStream redirectFrom);

        @Override
        void close() throws IOException, DeviceGoneException, InterruptedException;

    }

    private static class NullOutputTarget extends ForStdoutAndStderr {
        static final NullOutputTarget INSTANCE = new NullOutputTarget();

        @Override
        OutputHandle openOutput(IDevice device) {
            return new EphemeralOutputHandle("/dev/null");
        }
    }

    private static class StdoutRedirOutputTarget extends ForStderr {
        static final StdoutRedirOutputTarget INSTANCE = new StdoutRedirOutputTarget();

        @Override
        OutputHandle openOutput(IDevice device) {
            return new EphemeralOutputHandle("&1");
        }
    }

    private static class NoRedirTarget extends ForStdoutAndStderr {
        static final NoRedirTarget INSTANCE = new NoRedirTarget();

        @Override
        OutputHandle openOutput(IDevice device) {
            return new NoRedirectHandle();
        }
    }

    private static class StreamOutputTarget extends ForStdoutAndStderr {
        private final OutputStream stream;

        public StreamOutputTarget(OutputStream stream) {
            this.stream = stream;
        }

        @Override
        OutputHandle openOutput(IDevice device) {
            return new OutputHandle() {
                final DeviceTempFile tempFile = new DeviceTempFile(device);

                @Override
                public String getRedirectString(StdStream redirectFrom) {
                    return redirectFrom.getId() + ">" + tempFile.getPath();
                }

                @Override
                public void close() throws DeviceGoneException, InterruptedException, IOException {
                    try (DeviceTempFile theTempFile = tempFile) {
                        theTempFile.copyContentsTo(stream);
                    }
                }
            };
        }
    }

    private static class EphemeralOutputHandle implements OutputHandle {
        private final String redirectTarget;

        public EphemeralOutputHandle(String redirectTarget) {
            this.redirectTarget = redirectTarget;
        }

        @Override
        public String getRedirectString(StdStream redirectFrom) {
            return redirectFrom.getId() + ">" + redirectTarget;
        }

        @Override
        public void close() {
            // Nothing to close as this only exists as a redirect target.
        }
    }

    private static class NoRedirectHandle implements OutputHandle {

        @Override
        public void close() {
            // Nothing to close as this only exists as a redirect target.
        }

        @Override
        public String getRedirectString(StdStream redirectFrom) {
            // Don't do redirection at all
            return "";
        }
    }

    // A type check test, uncomment to see
    //    void stdout(ForStdout s) {}
    //    void stderr(ForStderr s) {}
    //
    //    void testCompilation() {
    //        stderr(new ForStderr() {
    //            @Override
    //            OutputHandle openOutput(IDevice device) {
    //                return null;
    //            }
    //        });
    //        stderr(new ForStdoutAndStderr() {
    //            @Override
    //            OutputHandle openOutput(IDevice device) {
    //                return null;
    //            }
    //        });
    //        stderr(new ForStdout() {  // Still compiles, I don't think it is possible to implement without multiple
    //            // inheritance or sealed classes :( Interfaces won't work as anyone can implement an interface
    //            @Override
    //            OutputHandle openOutput(IDevice device) {
    //                return null;
    //            }
    //        });
    //
    //        stdout(new ForStderr() {  // Does not compile :)
    //            @Override
    //            OutputHandle openOutput(IDevice device) {
    //                return null;
    //            }
    //        });
    //         stdout(new ForStdoutAndStderr() {
    //             @Override
    //             OutputHandle openOutput(IDevice device) {
    //                 return null;
    //             }
    //         });
    //         stdout(new ForStdout() {
    //             @Override
    //             OutputHandle openOutput(IDevice device) {
    //                 return null;
    //             }
    //         });
    //     }

}

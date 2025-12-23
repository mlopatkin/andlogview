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

import com.google.common.base.Throwables;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * Base class for exceptions reported while managing SDKs. These exceptions always carry user-readable failure messages.
 */
public class SdkException extends IOException {
    public SdkException(String message) {
        super(message);
    }

    private SdkException(String message, Throwable cause) {
        super(message, cause);
    }

    @FormatMethod
    public SdkException(@FormatString String format, Object... args) {
        super(String.format(format, sanitizeArgs(args)));
    }

    @Override
    public String getMessage() {
        return Objects.requireNonNull(super.getMessage());
    }

    @FormatMethod
    public static SdkException rethrow(
            Throwable cause,
            @FormatString String format,
            Object... args
    ) throws SdkException {
        Throwables.throwIfUnchecked(cause);
        Throwables.throwIfInstanceOf(cause, SdkException.class);

        throw new SdkException(String.format(format, sanitizeArgs(args)), cause);
    }

    private static Object[] sanitizeArgs(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof File) {
                args[i] = ((File) args[i]).getAbsolutePath();
            }
        }
        return args;
    }
}

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

package name.mlopatkin.andlogview.config;

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;

public class InvalidJsonContentException extends Exception {
    public InvalidJsonContentException(String message) {
        super(message);
    }

    @FormatMethod
    public InvalidJsonContentException(@FormatString String message, Object... args) {
        super(String.format(message, args));
    }

    public InvalidJsonContentException(String message, Throwable cause) {
        super(message, cause);
    }
}

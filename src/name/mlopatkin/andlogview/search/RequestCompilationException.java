/*
 * Copyright 2013 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.search;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.regex.PatternSyntaxException;

public class RequestCompilationException extends Exception {
    private String request;
    private final @Nullable PatternSyntaxException cause;
    private int startPos = -1;
    private int endPos = -1;

    public RequestCompilationException(String message, String request) {
        this(message, request, null);
        startPos = 0;
        endPos = request.length();
    }

    public RequestCompilationException(String message, String request,
            @Nullable PatternSyntaxException cause) {
        super(message, cause);
        this.request = request;
        this.cause = cause;
        if (cause != null && cause.getIndex() != -1) {
            startPos = cause.getIndex();
            endPos = cause.getIndex() + 1;
        }
    }

    public final String getRequestValue() {
        return request;
    }

    public final void setRequestValue(String request) {
        this.request = request;
        if (startPos != -1) {
            if (cause != null) {
                ++startPos;
                ++endPos;
            } else {
                endPos = request.length();
            }
        }
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }
}

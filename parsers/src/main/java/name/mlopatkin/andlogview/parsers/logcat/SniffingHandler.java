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

package name.mlopatkin.andlogview.parsers.logcat;

import name.mlopatkin.andlogview.parsers.ParserControl;

class SniffingHandler implements LogcatParseEventsHandler {
    public static final int NO_LIMIT = Integer.MAX_VALUE;

    private final int lookaheadLimit;

    private int linesConsumed;
    private boolean parsed;

    public SniffingHandler() {
        this(NO_LIMIT);
    }

    public SniffingHandler(int lookaheadLimit) {
        this.lookaheadLimit = lookaheadLimit;
    }

    @Override
    public final ParserControl logRecord(String message) {
        return logRecord();
    }

    protected ParserControl logRecord() {
        parsed = true;
        return ParserControl.stop();
    }

    @Override
    public ParserControl lineConsumed() {
        ++linesConsumed;
        if (linesConsumed > lookaheadLimit) {
            return ParserControl.stop();
        }
        return ParserControl.proceed();
    }

    @Override
    public ParserControl unparseableLine(CharSequence line) {
        if (linesConsumed >= lookaheadLimit) {
            return ParserControl.stop();
        }
        return ParserControl.proceed();
    }

    public boolean hasParsed() {
        return parsed;
    }
}

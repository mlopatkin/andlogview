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

package name.mlopatkin.andlogview.parsers;

/**
 * A skeleton implementation of the {@link BasePushParser} that handles most basic contract with regard to stopping.
 * <p>
 * After the parser returns false from {@link #nextLine(CharSequence)}, it no longer process any lines.
 */
public abstract class AbstractBasePushParser implements BasePushParser {
    private boolean shouldStop;

    @Override
    public final boolean nextLine(CharSequence line) {
        if (!shouldStop) {
            onNextLine(line);
        }
        return !shouldStop;
    }

    /**
     * Returns true if the parsing has been stopped.
     *
     * @return true if the parsing has been stopped
     */
    protected final boolean shouldStop() {
        return shouldStop;
    }

    /**
     * Can be called from {@link #onNextLine(CharSequence)} to indicate that the parsing must be stopped if the
     * parameter is true.
     *
     * @param shouldStop if true then the parsing stops
     */
    protected final void stopIf(boolean shouldStop) {
        this.shouldStop |= shouldStop;
    }

    /**
     * Can be called from {@link #onNextLine(CharSequence)} to indicate that the parsing must be stopped.
     */
    protected final void stop() {
        shouldStop = true;
    }

    /**
     * Can be called from {@link #onNextLine(CharSequence)} to indicate that the parsing must be stopped if the
     * parameter is false.
     *
     * @param shouldProceed if false then the parsing stops
     */
    protected final void stopUnless(boolean shouldProceed) {
        shouldStop |= !shouldProceed;
    }

    /**
     * Called if the parsing is not stopped yet and the parser is fed with the new line.
     *
     * @param line the line to parse
     */
    protected abstract void onNextLine(CharSequence line);

}

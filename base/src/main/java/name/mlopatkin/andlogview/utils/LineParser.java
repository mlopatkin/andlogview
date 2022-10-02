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

package name.mlopatkin.andlogview.utils;

import com.google.common.base.MoreObjects;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A simple state-machine-based push parser that operates on lines. When the current state receives a next line, it can
 * return the next state to proceed following lines.
 */
public class LineParser {
    /**
     * A single state of the parser. The state is typically implemented as a lambda assigned to a variable.
     */
    @FunctionalInterface
    public interface State {
        /**
         * Called when this is a current state and the parser receives a new line. This method should return a new
         * state. As a workaround from the fact that lambdas have no {@code this}, implementation should return
         * {@link LineParser#currentState()} from this method to stay in the current state.
         *
         * @param line the new line
         * @return the new state or {@link LineParser#currentState()} to stay in this state
         */
        @Nullable State nextLine(String line);
    }

    private State state;

    /**
     * Constructs a new LineParser
     *
     * @param initialState the initial state of the parser
     */
    public LineParser(State initialState) {
        this.state = initialState;
    }

    /**
     * Pushes a new line into the parser. This invokes {@link State#nextLine(String)} of the current state and update
     * the current state according to the result afterwards.
     *
     * @param line the new line
     */
    public void nextLine(String line) {
        state = MoreObjects.firstNonNull(state.nextLine(line), state);
    }

    /**
     * Returns the special state value that the Parser recognizes as "stay in the current state".
     * @return the special state value
     */
    public static @Nullable State currentState() {
        return null;
    }

    /**
     * Returns a state that always stays in itself, the so-called "sink".
     * @return the sink state
     */
    public static State sinkState() {
        return line -> currentState();
    }
}

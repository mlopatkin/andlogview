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

public class LineParser {
    @FunctionalInterface
    public interface State {
        @Nullable State nextLine(String line);
    }

    private State state;

    public LineParser(State initialState) {
        this.state = initialState;
    }

    public void nextLine(String line) {
        state = MoreObjects.firstNonNull(state.nextLine(line), state);
    }
}

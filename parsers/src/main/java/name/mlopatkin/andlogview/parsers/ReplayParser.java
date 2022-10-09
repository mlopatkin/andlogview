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

package name.mlopatkin.andlogview.parsers;

import java.util.ArrayList;
import java.util.List;

/**
 * Replay parser wraps an existing parser and records all processed lines. Processed lines can then be replayed into
 * some other parser. The primary use case is format autodetect where you want to process some lines to determine
 * the format and then perform the actual parsing.
 *
 * @param <T> the type of the delegate parser
 */
public class ReplayParser<T extends BasePushParser> implements BasePushParser {
    private static final long NO_LIMIT = Integer.MAX_VALUE + 1L;

    private final List<CharSequence> replayBuffer = new ArrayList<>();
    private final long replayLimit;
    private final T delegate;

    /**
     * Creates an instance of the replay parser with limited replay buffer. When the buffer grows to the limit, no new
     * lines are accepted or forwarded to the delegate.
     *
     * @param replayLimit the maximal number of lines stored in the replay buffer
     * @param delegate the delegate parser
     */
    public ReplayParser(int replayLimit, T delegate) {
        this((long) replayLimit, delegate);
    }

    /**
     * Creates an instance of the replay parser with unlimited replay buffer.
     *
     * @param delegate the delegate parser
     */
    public ReplayParser(T delegate) {
        this(NO_LIMIT, delegate);
    }

    private ReplayParser(long replayLimit, T delegate) {
        this.replayLimit = replayLimit;
        this.delegate = delegate;
    }

    @Override
    public boolean nextLine(CharSequence line) {
        if (hasMoreRoom()) {
            replayBuffer.add(line);
            return delegate.nextLine(line) && hasMoreRoom();
        }
        return false;
    }

    private boolean hasMoreRoom() {
        return replayBuffer.size() < replayLimit;
    }

    /**
     * Forwards stored lines to the given parser. If the parser stops before all stored lines are processed then the
     * rest is not forwarded and this method returns {@code false}.
     *
     * @param parser the parser to replay the input into
     * @return {@code true} if {@code parser} accepted all input, {@code false} otherwise
     */
    public boolean replayInto(BasePushParser parser) {
        for (CharSequence line : replayBuffer) {
            if (!parser.nextLine(line)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void close() {
        replayBuffer.clear();
        delegate.close();
    }

    /**
     * Returns the delegate submitted to constructor.
     *
     * @return the delegate
     */
    public T getDelegate() {
        return delegate;
    }
}

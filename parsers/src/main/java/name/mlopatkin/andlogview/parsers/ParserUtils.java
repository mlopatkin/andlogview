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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.stream.Stream;

/**
 * Utility methods to deal with push parsers.
 */
public final class ParserUtils {
    private ParserUtils() {}

    @FunctionalInterface
    public interface ThrowingSupplier<T, E extends Exception> {
        @Nullable T get() throws E;
    }

    /**
     * Feeds the lines produced by {@code supplier} into the {@code parser} until the supplier returns {@code null} or
     * the parser signals stop (returns {@code false} from {@link BasePushParser#nextLine(CharSequence)}).
     *
     * @param parser the parser to feed
     * @param supplier the supplier to produce lines to feed the parser
     * @param <E> the (optional) exception type of the supplier's
     * @return {@code true} if the parser successfully consumed all output of the supplier, or {@code false} if the
     *         parser stopped
     * @throws E if the supplier's {@code get()} throws
     */
    public static <E extends Exception> boolean readInto(BasePushParser parser,
            ThrowingSupplier<? extends CharSequence, E> supplier) throws E {
        @Nullable CharSequence line;
        while ((line = supplier.get()) != null) {
            if (!parser.nextLine(line)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Feeds the stream into the {@code parser} until the stream ends or the parser signals stop (returns {@code false}
     * from {@link BasePushParser#nextLine(CharSequence)}).
     *
     * @param parser the parser to feed
     * @param lines the lines to feed the parser
     * @return {@code true} if the parser successfully consumed all lines, or {@code false} if the parser stopped
     */
    public static boolean readInto(BasePushParser parser, Stream<? extends CharSequence> lines) {
        var iterator = lines.iterator();
        while (iterator.hasNext()) {
            if (!parser.nextLine(iterator.next())) {
                return false;
            }
        }
        return true;
    }
}

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

import java.util.function.Function;

/**
 * A base type for the format detector. After detecting the format of the input, the format detector can create parsers
 * that handle the detected format.
 *
 * @param <H> the type of the handler used by the parsers created by this detector.
 */
public interface FormatSniffer<H extends PushParser.ParseEventsHandler> {
    /**
     * Returns the current status of format detection.
     *
     * @return {@code true} if the format is detected, {@code false} otherwise
     */
    boolean isFormatDetected();

    /**
     * Returns a string description of the detected format or readable explanation that format is not detected.
     *
     * @return the string description of the detected format
     */
    String getDetectedFormatDescription();

    /**
     * Creates a parser capable of handling the detected format, with the given handler
     *
     * @param handler the format handler for parser to feed
     * @param <V> the actual type of the handler
     * @return the parser
     */
    <V extends H> PushParser<V> createParser(V handler);

    /**
     * A helper method to handle the common task of replaying the input used to detect the format into a newly created
     * parser. The input is replayed immediately after the parser is created. The return value is ignored. The factory
     * function is typically some overload of {@link FormatSniffer#createParser(PushParser.ParseEventsHandler)}.
     *
     * @param r the replay parser used to capture the input when sniffing
     * @param factory the factory method to create the parser
     * @param handler the handler to for the newly created parser to feed
     * @param <H> the actual type of handler
     * @param <V> the actual type of the returned push parser
     * @return the push parser
     */
    static <H extends PushParser.ParseEventsHandler, V extends PushParser<H>> V createAndReplay(
            ReplayParser<?> r, Function<H, V> factory, H handler) {
        // I didn't find a way to generify FormatSniffer itself to provide some subtype of PushParser, as we cannot have
        // a parameterized generic like P<V> where P extends PushParser and V extends H. However, it is easy with
        // functions, you just invoke this as createAndReplay(r, sniffer::createParser, h).
        V parser = factory.apply(handler);
        r.replayInto(parser);
        return parser;
    }
}

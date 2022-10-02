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

/**
 * Push parsers for supported logcat formats.
 */
public final class LogcatParsers {
    private LogcatParsers() {}

    /**
     * Creates a parser for the {@code brief} format.
     *
     * @param eventsHandler the handler of parse events
     * @return the push parser that processes logs in brief format
     */
    public static <H extends LogcatParseEventsHandler> LogcatPushParser<H> brief(H eventsHandler) {
        return new LogcatPushParser<>(Format.BRIEF, eventsHandler);
    }

    /**
     * Creates a parser for the {@code long} format. Due to the clash with the Java long type this method cannot be
     * named {@code long}.
     *
     * @param eventsHandler the handler of parse events
     * @return the push parser that processes logs in brief format
     */
    public static <H extends LogcatParseEventsHandler> LogcatPushParser<H> logcatLong(H eventsHandler) {
        return new LogcatPushParser<>(Format.LONG, eventsHandler);
    }

    /**
     * Creates a parser for the {@code process} format.
     *
     * @param eventsHandler the handler of parse events
     * @return the push parser that processes logs in process format
     */
    public static <H extends LogcatParseEventsHandler> LogcatPushParser<H> process(H eventsHandler) {
        return new LogcatPushParser<>(Format.PROCESS, eventsHandler);
    }

    /**
     * Creates a parser for the {@code raw} format.
     *
     * @param eventsHandler the handler of parse events
     * @return the push parser that processes logs in raw format
     */
    public static <H extends LogcatParseEventsHandler> LogcatPushParser<H> raw(H eventsHandler) {
        return new LogcatPushParser<>(Format.RAW, eventsHandler);
    }

    /**
     * Creates a parser for the Android Studio {@code logcat} format.
     *
     * @param eventsHandler the handler of parse events
     * @return the push parser that processes logs in Android Studio format
     */
    public static <H extends LogcatParseEventsHandler> LogcatPushParser<H> androidStudio(H eventsHandler) {
        return new LogcatPushParser<>(Format.STUDIO, eventsHandler);
    }

    /**
     * Creates a parser for the {@code tag} format.
     *
     * @param eventsHandler the handler of parse events
     * @return the push parser that processes logs in tag format
     */
    public static <H extends LogcatParseEventsHandler> LogcatPushParser<H> tag(H eventsHandler) {
        return new LogcatPushParser<>(Format.TAG, eventsHandler);
    }

    /**
     * Creates a parser for the {@code thread} format.
     *
     * @param eventsHandler the handler of parse events
     * @return the push parser that processes logs in thread format
     */
    public static <H extends LogcatParseEventsHandler> LogcatPushParser<H> thread(H eventsHandler) {
        return new LogcatPushParser<>(Format.THREAD, eventsHandler);
    }

    /**
     * Creates a parser for the {@code threadtime} format.
     *
     * @param eventsHandler the handler of parse events
     * @return the push parser that processes logs in threadtime format
     */
    public static <H extends LogcatParseEventsHandler> LogcatPushParser<H> threadTime(H eventsHandler) {
        return new LogcatPushParser<>(Format.THREADTIME, eventsHandler);
    }

    /**
     * Creates a parser for the {@code time} format.
     *
     * @param eventsHandler the handler of parse events
     * @return the push parser that processes logs in time format
     */
    public static <H extends LogcatParseEventsHandler> LogcatPushParser<H> time(H eventsHandler) {
        return new LogcatPushParser<>(Format.TIME, eventsHandler);
    }
}

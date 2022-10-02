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

/**
 * Parsers for the logcat output. The "adb logcat" can output logs in multitude of formats. At the current time,
 * Andlogview supports some subset of these with the ultimate goal of supporting everything ever written.
 * <h2>Format overview</h2>
 * Most of the logcat output formats are text-based. There is a binary output format, but it isn't very well documented
 * and there are some difficulties in parsing it, so it isn't supported in Andlogview. All text-based formats except
 * one ({@code long}) are single-line-per-entry. Additionally, there could be "control lines", looking like
 * {@code --------- beginning of main}. Control lines may signal about the log buffer to which the following log entries
 * belong.
 * <h2>Architecture</h2>
 * All parsers in this package are push parsers, so they send events (call methods) of a user-provided event handler
 * according to the parsing state. Some common events are "log record parsed", "a line was not parsed",
 * "document ended". Parsers are fed with lines, one by one.
 */
package name.mlopatkin.andlogview.parsers.logcat;

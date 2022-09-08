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

package name.mlopatkin.andlogview.liblogcat;

import java.util.Date;

/**
 * The timestamp that is shown in the logcat output. Logcat can produce timestamps in multiple formats:
 * <ul>
 *     <li>The default {@code time} format: {@code 01-09 17:17:12.666}. It is supported since the very first version of
 *     Android. This is a wall-clock time, without year information.</li>
 *     <li>The {@code epoch}: {@code 1641745032.784}. Seconds since 1970-01-01 00:00:00+0 with milliseconds
 *     precision. This is unlikely to be negative in practice.</li>
 *     <li>The {@code monotonic}: {@code 8767.799}. CPU seconds since last boot, supposedly not counting time spend in
 *     deep sleep. Interestingly enough, this timestamp can be negative: {@code -1.587}</li>
 * </ul>
 * The last specified format wins. Moreover, there are additional modifiers that affect the display of these formats.
 * <p>
 * The {@code usec} adds microsecond precision to all three, so {@code .666} becomes {@code .666432}.
 * </p>
 * <p>Other modifiers only affect {@code time}
 * format. A few allow to deal with the time zones:
 * <ul>
 *     <li>{@code zone} - display the timezone offset of the local timezone used by device. Most likely it is a
 *     purely display-time thing, the actual selected timezone at the time of writing the log doesn't matter (?).</li>
 *     <li>{@code UTC} - display time in the UTC timezone.</li>
 *     <li>{@code "<zone>"}, e.g. "{@code "Europe/Moscow"} - display time in the timezone named {@code <zone>}.</li>
 * </ul>
 * Using any of these adds the timezone offset to the timestamp: {@code 01-09 16:56:59.976 +0300}.
 * </p>
 * <p>
 * The last modifier is {@code year}, it also only affects {@code time} format. This modifier shows the year of the
 * timestamp: {@code 2022-01-09 14:56:59.970}.
 * </p>
 * <p>
 * Of course, it is possible to have {@code time,zone,year,usec} simultaneously:
 * {@code 2022-01-09 14:56:59.970708 +0100}
 * </p>
 * <p>
 * The versatility of the format makes it inconvenient to use standard Java types.
 * </p>
 */
public class Timestamp implements Comparable<Timestamp> {
    // It is possible for the different time formats to be mixed within the same data source, for example, when
    // combining multiple log files. However, in this case it may be hard to sort timestamps correctly. The monotonic
    // timestamp cannot be converted to other formats as there is no "baseline". It is hard to automatically distinguish
    // epoch from the monotonic, though the former is likely to have much larger values. Timestamps with and without the
    // year are also somewhat problematic, especially when the log crosses the year boundary.

    // For now this class is a mere wrapper for the java.util.Date. It cannot adequately model the complex domain
    // described in its javadoc. The abstraction is going to evolve.
    private final Date dateTime;

    public Timestamp(Date dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public int compareTo(Timestamp o) {
        return dateTime.compareTo(o.dateTime);
    }

    public Date asDate() {
        return dateTime;
    }
}

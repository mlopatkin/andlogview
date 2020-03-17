/*
 * Copyright 2018 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.liblogcat;

import com.google.common.base.CharMatcher;

import javax.annotation.Nullable;

/**
 * Push-reader that tries to determine format of the log.
 */
public interface LogFormatSniffer {
    /**
     * Feeds the line into the reader.
     *
     * @param nextLine the line
     * @return {@code true} if the line matches format recognizable by this reader
     */
    boolean push(@Nullable String nextLine);

    /**
     * Helper base class that rejects nulls, empty and whitespace-only strings.
     */
    abstract class SkipNullOrEmpty implements LogFormatSniffer {
        @Override
        public final boolean push(@Nullable String nextLine) {
            if (nextLine == null) {
                return false;
            }
            String trimmedLine = CharMatcher.whitespace().trimFrom(nextLine);
            if (trimmedLine.isEmpty()) {
                return false;
            }
            return pushNonEmpty(trimmedLine);
        }

        protected abstract boolean pushNonEmpty(String nextLine);
    }
}

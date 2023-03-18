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

package name.mlopatkin.andlogview.parsers.logcat;

import name.mlopatkin.andlogview.logmodel.LogRecord;

import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for the logcat parser compatibility testing. A parameterized test is expected to have the parameters:
 * <ol>
 *     <li>{@link Format} - the format of the log lines</li>
 *     <li>List&lt;{@linkplain LogRecord }> - the log records that the lines contain</li>
 *     <li>{@link Eoln} - the end of line char sequence used in lines</li>
 *     <li>List&lt;String> - the log records as a sequence of lines, each ending with the given Eoln</li>
 * </ol>
 */
@ArgumentsSource(LogcatCompatProvider.class)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface LogcatCompatSource {
    /**
     * The path to the JSON resource describing the test suite.
     */
    String path();

    /**
     * The list of the formats to test on. Defaults to all available in the suite.
     */
    Format[] formats() default {};

    /**
     * The list of all EOLN sequences to test with. Defaults to only NONE.
     */
    Eoln[] eolns() default { Eoln.NONE };
}

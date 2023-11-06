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

package name.mlopatkin.andlogview.test;

import name.mlopatkin.andlogview.utils.MyFutures;

/**
 * Testing facilities for threads
 */
public final class ThreadTestUtils {
    private ThreadTestUtils() {}

    public static void withUncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler,
            MyFutures.ThrowingRunnable r) throws Exception {
        var prevHandler = Thread.currentThread().getUncaughtExceptionHandler();
        Thread.currentThread().setUncaughtExceptionHandler(handler);
        try {
            r.run();
        } finally {
            Thread.currentThread().setUncaughtExceptionHandler(prevHandler);
        }
    }

    public static void withEmptyUncaughtExceptionHandler(MyFutures.ThrowingRunnable r) throws Exception {
        withUncaughtExceptionHandler((t, e) -> {}, r);
    }
}

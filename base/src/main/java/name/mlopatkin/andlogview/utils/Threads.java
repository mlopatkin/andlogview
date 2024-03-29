/*
 * Copyright 2014 Mikhail Lopatkin
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

import java.util.concurrent.ThreadFactory;

public final class Threads {
    private Threads() {}

    /**
     * Returns a thread factory that constructs threads with a given name. It should be used in singleThreadExecutor
     *
     * @param name the name of the created thread
     * @return the thread factory that creates threads with the given name
     */
    public static ThreadFactory withName(final String name) {
        return r -> new Thread(r, name);
    }
}

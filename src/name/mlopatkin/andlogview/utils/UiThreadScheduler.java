/*
 * Copyright 2021 Mikhail Lopatkin
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

import java.util.concurrent.ScheduledExecutorService;

/**
 * The simpler analog to the {@link ScheduledExecutorService} that allows to post delayed tasks to the UI thread.
 */
public interface UiThreadScheduler {
    /**
     * Posts a delayed task to the UI thread.
     * @param task the task to run
     * @param delayMs the delay in milliseconds
     * @return the handle to cancel pending task
     */
    Cancellable postDelayedTask(Runnable task, int delayMs);
}

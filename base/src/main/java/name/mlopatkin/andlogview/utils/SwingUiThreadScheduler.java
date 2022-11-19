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

import com.google.common.primitives.Ints;

import java.time.Duration;

import javax.swing.Timer;

public class SwingUiThreadScheduler implements UiThreadScheduler {
    @Override
    public Cancellable postDelayedTask(Runnable task, int delayMs) {
        Timer timer = new Timer(delayMs, event -> task.run());
        timer.setRepeats(false);
        timer.start();
        return new CancellableTask(timer);
    }

    @Override
    public Cancellable postRepeatableTask(Runnable task, Duration interval) {
        int delayMs = Ints.checkedCast(interval.toMillis());
        Timer timer = new Timer(delayMs, event -> task.run());
        timer.setRepeats(true);
        return new CancellableTask(timer);
    }

    private static class CancellableTask implements Cancellable {
        private final Timer timer;

        public CancellableTask(Timer timer) {
            this.timer = timer;
        }

        @Override
        public boolean cancel() {
            if (timer.isRunning()) {
                timer.stop();
                return true;
            }
            return false;
        }
    }
}

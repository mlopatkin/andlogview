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

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.time.Duration;
import java.util.PriorityQueue;

/**
 * Test implementation of the {@link UiThreadScheduler}. Tasks are executed manually.
 */
public class MockUiThreadScheduler implements UiThreadScheduler {
    private final PriorityQueue<ScheduledTask> taskQueue = new PriorityQueue<>();
    private long currentTimeMs;

    /**
     * {@inheritDoc}
     * Note that the task will not be executed inside this method even if the {@code delayMs == 0}.
     */
    @Override
    public Cancellable postDelayedTask(Runnable task, int delayMs) {
        ScheduledTask scheduledTask = new ScheduledTask(task, currentTimeMs + delayMs);
        taskQueue.offer(scheduledTask);
        return scheduledTask;
    }

    /**
     * Advances internal timer by {@code delayMs} milliseconds. Executes all tasks that are scheduled to run in this
     * interval.
     *
     * @param delayMs the amount of time to advance
     */
    public void advance(long delayMs) {
        // We cannot just advance currentTimeMs to newCurrentTime immediately. It is possible for tasks to schedule
        // other tasks so the currentTimeMs should be consistent with the deadline of the task executed. Otherwise,
        // tasks will be scheduled to much later time than in real life.
        long newCurrentTime = currentTimeMs + delayMs;
        while (!taskQueue.isEmpty() && taskQueue.peek().deadlineMs <= newCurrentTime) {
            executeSingleTask();
        }
        currentTimeMs = newCurrentTime;
    }

    private void executeSingleTask() {
        ScheduledTask task = taskQueue.poll();
        if (task == null) {
            return;
        }
        // Order is important there. We advance task first so tasks scheduled in task.run() pick up a proper
        // currentTimeMs value.
        currentTimeMs = Math.max(currentTimeMs, task.deadlineMs);
        task.run();
    }

    /**
     * Processes all posted tasks and tasks that these task may post.
     */
    public void drainAll() {
        while (!taskQueue.isEmpty()) {
            executeSingleTask();
        }
    }

    @Override
    public Cancellable postRepeatableTask(Runnable task, Duration interval) {
        return new RepeatingTask(task, interval).start();
    }

    private class ScheduledTask implements Cancellable, Comparable<ScheduledTask>, Runnable {
        final long deadlineMs;
        final Runnable task;

        private ScheduledTask(Runnable task, long deadlineMs) {
            this.deadlineMs = deadlineMs;
            this.task = task;
        }

        @Override
        public int compareTo(ScheduledTask o) {
            return Long.compare(deadlineMs, o.deadlineMs);
        }

        @Override
        public boolean cancel() {
            return taskQueue.remove(this);
        }

        @Override
        public void run() {
            task.run();
        }
    }

    private class RepeatingTask implements Runnable {
        private final int delayMs;
        private final Runnable task;
        private @MonotonicNonNull Cancellable lastScheduledInstance;

        private RepeatingTask(Runnable task, Duration delay) {
            this.task = task;
            this.delayMs = (int) delay.toMillis();
        }

        @Override
        public void run() {
            task.run();
            lastScheduledInstance = postDelayedTask(this, delayMs);
        }

        public Cancellable start() {
            assert lastScheduledInstance == null;
            lastScheduledInstance = postDelayedTask(this, delayMs);
            return () -> {
                assert lastScheduledInstance != null;
                return lastScheduledInstance.cancel();
            };
        }
    }
}

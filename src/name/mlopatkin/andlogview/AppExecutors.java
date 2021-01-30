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

package name.mlopatkin.andlogview;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import dagger.Module;
import dagger.Provides;

import java.awt.EventQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Global application executors (aka well-known thread and thread pools).
 */
public class AppExecutors {
    /**
     * The name of the UI thread-confined executor. Submitted tasks are executed in FIFO order on the AWT's Event
     * Dispatching Thread.
     */
    public static final String UI_EXECUTOR = "ui_executor";
    /**
     * The name of the thread pool for the file operations. There are no implicit ordering guarantees for the tasks
     * submitted to it.
     */
    public static final String FILE_EXECUTOR = "file_executor";

    @Module
    static class ExecutorsModule {

        @Provides
        @Singleton
        @Named(UI_EXECUTOR)
        public static Executor getUiExecutor() {
            return EventQueue::invokeLater;
        }

        @Provides
        @Singleton
        @Named(FILE_EXECUTOR)
        public static Executor getFileExecutor(@Named(FILE_EXECUTOR) ExecutorService executorService) {
            return executorService;
        }

        @Provides
        @Singleton
        @Named(FILE_EXECUTOR)
        public static ExecutorService getFileExecutorService() {
            return Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("file-thread-%d").build());
        }
    }
}

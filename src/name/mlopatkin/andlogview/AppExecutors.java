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

import name.mlopatkin.andlogview.base.concurrent.SequentialExecutor;
import name.mlopatkin.andlogview.utils.SwingUiThreadScheduler;
import name.mlopatkin.andlogview.utils.UiThreadScheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

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
    abstract static class ExecutorsModule {
        @Binds
        @Named(UI_EXECUTOR)
        public abstract Executor getUiExecutor(@Named(UI_EXECUTOR) SequentialExecutor uiExecutor);

        @Provides
        @Singleton
        @Named(UI_EXECUTOR)
        public static SequentialExecutor getUiSequentialExecutor() {
            return SequentialExecutor.edt();
        }

        @Binds
        @Singleton
        @Named(FILE_EXECUTOR)
        public abstract Executor getFileExecutor(@Named(FILE_EXECUTOR) ExecutorService executorService);

        @Provides
        @Singleton
        @Named(FILE_EXECUTOR)
        public static ExecutorService getFileExecutorService() {
            return Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("file-thread-%d").build());
        }

        @Provides
        public static UiThreadScheduler getUiTimer() {
            return new SwingUiThreadScheduler();
        }
    }
}

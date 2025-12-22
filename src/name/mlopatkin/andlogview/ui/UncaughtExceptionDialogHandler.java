/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview.ui;

import name.mlopatkin.andlogview.ErrorDialogsHelper;
import name.mlopatkin.andlogview.thirdparty.systemutils.SystemUtils;

import org.jspecify.annotations.Nullable;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * An implementation of {@link UncaughtExceptionHandler} that shows an error dialog.
 */
public class UncaughtExceptionDialogHandler implements UncaughtExceptionHandler {
    private final Component mainAppFrame;
    private final @Nullable UncaughtExceptionHandler baseHandler;

    private UncaughtExceptionDialogHandler(Component mainAppFrame, @Nullable UncaughtExceptionHandler baseHandler) {
        this.mainAppFrame = mainAppFrame;
        this.baseHandler = baseHandler;
    }

    private void showExceptionDialog(Throwable failure) {
        ErrorDialogsHelper.showError(mainAppFrame,
                "<html>Unhandled exception occurred. Please collect log file at<br>"
                + new File(SystemUtils.getJavaIoTmpDir(), "logview.log").getAbsolutePath()
                + "<br>and send it to the authors, then restart the program", failure);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (baseHandler != null) {
            baseHandler.uncaughtException(t, e);
        }

        try {
            if (EventQueue.isDispatchThread()) {
                showExceptionDialog(e);
            } else {
                EventQueue.invokeLater(() -> showExceptionDialog(e));
            }
        } catch (Throwable th) { // OK to catch Throwable here
            // Let the base handler do the logging of this exception too.
            if (baseHandler != null) {
                baseHandler.uncaughtException(t, th);
            }
        }
    }

    public void uninstall() {
        if (Thread.getDefaultUncaughtExceptionHandler() == this) {
            Thread.setDefaultUncaughtExceptionHandler(baseHandler);
        }
    }

    public static UncaughtExceptionDialogHandler install(Component mainAppFrame) {
        var handler = new UncaughtExceptionDialogHandler(mainAppFrame, Thread.getDefaultUncaughtExceptionHandler());
        Thread.setDefaultUncaughtExceptionHandler(handler);
        return handler;
    }
}

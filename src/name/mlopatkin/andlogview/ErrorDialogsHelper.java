/*
 * Copyright 2011 Mikhail Lopatkin
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

import name.mlopatkin.andlogview.widgets.dialogs.ErrorDialogWithDetails;
import name.mlopatkin.andlogview.widgets.dialogs.OptionPaneBuilder;

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;

import org.jspecify.annotations.Nullable;

import java.awt.Component;

/**
 * This class contains helper methods to show error dialogs
 * to the user.
 */
public class ErrorDialogsHelper {
    private ErrorDialogsHelper() {}

    @FormatMethod
    public static void showError(@Nullable Component owner, @FormatString String format, @Nullable Object... vals) {
        String message = String.format(format, vals);
        showError(owner, message);
    }

    public static void showError(@Nullable Component owner, String message) {
        OptionPaneBuilder.error("Error")
                .message(message)
                .show(owner);
    }

    public static void showError(@Nullable Component owner, String message, Throwable failure) {
        ErrorDialogWithDetails.show(owner, message, failure);
    }
}

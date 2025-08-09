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

package name.mlopatkin.andlogview.ui.logtable;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.utils.events.Observable;

import org.jspecify.annotations.Nullable;

import java.awt.Color;

/**
 * Implementation of this interface controls the appearance of the table model.
 */
public interface LogModelFilter {
    interface Observer {
        void onModelChange();
    }

    boolean shouldShowRecord(LogRecord record);

    @Nullable Color getHighlightColor(LogRecord record);

    Observable<Observer> asObservable();
}

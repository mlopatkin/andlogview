/*
 * Copyright 2020 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.logtable;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

/**
 * Performs final initialization steps on a LogTable: adds various listeners, adjust properties, etc.
 */
@LogTableScoped
class LogTableInitializer {
    private final LogTable incompleteTable;
    private final PopupMenu popupMenu;
    private boolean isInitialized;

    @Inject
    LogTableInitializer(LogTable incompleteTable, PopupMenu popupMenu) {
        this.incompleteTable = incompleteTable;
        this.popupMenu = popupMenu;
    }

    public LogTable completeInitialization() {
        Preconditions.checkState(!isInitialized, "Already initialized");
        isInitialized = true;
        popupMenu.attachToTable(incompleteTable);
        return incompleteTable;
    }
}

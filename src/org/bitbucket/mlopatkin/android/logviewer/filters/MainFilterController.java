/*
 * Copyright 2015 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.filters;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.FilteredLogModel;

import java.awt.Color;

import javax.annotation.Nullable;

/**
 * The filter controller of the main window.
 */
public class MainFilterController implements FilteredLogModel {

    @Override
    public boolean shouldShowRecord(LogRecord record) {
        return false;
    }

    @Nullable
    @Override
    public Color getHighlightColor(LogRecord record) {
        return null;
    }

    @Override
    public void addObserver(FilteredLogModel.Observer observer) {
    }

    @Override
    public void removeObserver(FilteredLogModel.Observer observer) {
    }
}

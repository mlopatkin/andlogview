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

package org.bitbucket.mlopatkin.android.logviewer.ui.indexframe;

import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogModelFilter;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogTable;

import dagger.Module;
import dagger.Provides;

import javax.annotation.Nullable;
import javax.inject.Named;

@Module
public class IndexFrameModule {

    private final IndexController indexController;
    private final PopupBuilder popupBuilder;
    private final LogModelFilter logModelFilter;

    public IndexFrameModule(IndexController indexController,
                            @Nullable PopupBuilder popupBuilder,
                            LogModelFilter logModelFilter) {
        this.indexController = indexController;
        this.popupBuilder = popupBuilder;
        this.logModelFilter = logModelFilter;
    }

    public IndexFrameModule(IndexController indexController, LogModelFilter logModelFilter) {
        this(indexController, null, logModelFilter);
    }

    @Provides
    @IndexFrameScoped
    @Named(IndexFrameComponent.FOR_INDEX_FRAME)
    LogTable getIndexWindowTable(LogRecordTableModel model) {
        return new LogTable(model, logModelFilter);
    }

    @Provides
    IndexTableColumnModel getColumnModel() {
        return new IndexTableColumnModel(null);
    }

    @Provides
    IndexController getIndexController() {
        return indexController;
    }

    @Provides
    @Nullable
    PopupBuilder getPopupBuilder() {
        return popupBuilder;
    }
}

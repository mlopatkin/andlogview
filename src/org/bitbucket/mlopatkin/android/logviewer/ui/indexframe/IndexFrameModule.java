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

import com.google.common.collect.ImmutableList;

import dagger.Module;
import dagger.Provides;

import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogModelFilter;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableColumnModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogRecordTableModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogTable;

import javax.annotation.Nullable;
import javax.inject.Named;

@Module
public class IndexFrameModule {
    private static final ImmutableList<Column> INDEX_FRAME_COLUMNS =
            ImmutableList.of(Column.INDEX, Column.TIME, Column.PID, Column.PRIORITY, Column.TAG, Column.MESSAGE);

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
        return LogTable.create(model, logModelFilter);
    }

    @Provides
    LogRecordTableColumnModel getColumnModel(LogRecordTableColumnModel.Factory factory) {
        return factory.create(null, INDEX_FRAME_COLUMNS);
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

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

package name.mlopatkin.andlogview.ui.indexfilter;

import static name.mlopatkin.andlogview.ui.mainframe.MainFrameDependencies.FOR_MAIN_FRAME;

import name.mlopatkin.andlogview.filters.ChildModelFilter;
import name.mlopatkin.andlogview.filters.CompoundFilterModel;
import name.mlopatkin.andlogview.filters.MutableFilterModel;
import name.mlopatkin.andlogview.ui.indexframe.AbstractIndexController;
import name.mlopatkin.andlogview.ui.indexframe.DaggerIndexFrameDi_IndexFrameComponent;
import name.mlopatkin.andlogview.ui.indexframe.IndexFrame;
import name.mlopatkin.andlogview.ui.indexframe.IndexFrameDi;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableModel;
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;

import java.awt.EventQueue;

import javax.inject.Named;
import javax.swing.JTable;

public class IndexFilterController extends AbstractIndexController implements AutoCloseable {
    private final MutableFilterModel filterModel;
    private final ChildModelFilter filter;
    private final IndexFrame frame;
    private final IndexFilter logModelFilter;

    @AssistedInject
    IndexFilterController(
            LogRecordTableModel logModel,
            DialogFactory dialogFactory,
            MutableFilterModel parentFilterModel,
            @Named(FOR_MAIN_FRAME) JTable mainTable,
            @Assisted ChildModelFilter filter) {
        super(mainTable);
        this.filterModel = parentFilterModel;

        this.filter = filter;
        logModelFilter = new IndexFilter(new CompoundFilterModel(parentFilterModel, filter.getFilters()));

        IndexFrameDi.IndexFrameComponent component = DaggerIndexFrameDi_IndexFrameComponent.builder()
                .logRecordTableModel(logModel)
                .dialogFactory(dialogFactory)
                .setIndexController(this)
                .setIndexFilter(logModelFilter)
                .build();
        frame = component.createFrame();
    }

    public void show() {
        // Postpone actual visibility change for two reasons:
        // 1. Main Frame may not be ready at this point (e.g. when restoring filters at startup).
        // 2. Client code may want to immediately disable the filter (?), post-ing avoids flickering.
        EventQueue.invokeLater(() -> frame.setVisible(true));
    }

    @Override
    public void close() {
        frame.dispose();
        logModelFilter.close();
    }

    @Override
    public void onWindowClosed() {
        filterModel.replaceFilter(filter, filter.disabled());
    }

    @AssistedFactory
    public interface Factory {
        IndexFilterController create(ChildModelFilter filter);
    }
}

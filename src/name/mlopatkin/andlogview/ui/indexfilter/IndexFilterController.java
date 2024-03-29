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

import name.mlopatkin.andlogview.filters.Filter;
import name.mlopatkin.andlogview.filters.FilterModel;
import name.mlopatkin.andlogview.ui.indexframe.AbstractIndexController;
import name.mlopatkin.andlogview.ui.indexframe.DaggerIndexFrameDi_IndexFrameComponent;
import name.mlopatkin.andlogview.ui.indexframe.IndexFrame;
import name.mlopatkin.andlogview.ui.indexframe.IndexFrameDi;
import name.mlopatkin.andlogview.ui.logtable.LogModelFilter;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameDependencies;

import java.awt.EventQueue;

import javax.inject.Inject;
import javax.swing.JTable;

public class IndexFilterController extends AbstractIndexController {
    private final FilterModel filterModel;
    private final Filter filter;
    private final IndexFrame frame;

    IndexFilterController(FilterModel filterModel, MainFrameDependencies dependencies, JTable mainTable,
            LogModelFilter mainFilter, Filter filter) {
        super(mainTable);
        this.filterModel = filterModel;

        this.filter = filter;
        IndexFrameDi.IndexFrameComponent component = DaggerIndexFrameDi_IndexFrameComponent.builder()
                .mainFrameDependencies(dependencies)
                .setIndexController(this)
                .setIndexFilter(new IndexFilter(mainFilter, filter))
                .build();
        frame = component.createFrame();
    }

    public void show() {
        // Postpone actual visibility change for two reasons:
        // 1. Main Frame may not be ready at this point (e.g. when restoring filters at startup).
        // 2. Client code may want to immediately disable the filter (?), post-ing avoids flickering.
        EventQueue.invokeLater(() -> frame.setVisible(true));
    }

    public void destroy() {
        frame.dispose();
    }

    @Override
    public void onWindowClosed() {
        filterModel.replaceFilter(filter, filter.disabled());
    }

    public static class Factory {
        private final MainFrameDependencies dependencies;
        private final FilterModel filterModel;

        @Inject
        public Factory(MainFrameDependencies dependencies, FilterModel filterModel) {
            this.dependencies = dependencies;
            this.filterModel = filterModel;
        }

        public IndexFilterController create(IndexFilterCollection owner, Filter filter) {
            // TODO dependency cycle, dangerous
            // MainFilterController -> IndexFilterCollection -> Factory -> MainFrameDependencies -> MainFilterController
            // We probably can break this by factoring Filter out of MainFilterController and making both Factory and
            // MainFilterController to depend on this new Filter
            return new IndexFilterController(
                    filterModel, dependencies, dependencies.getLogTable(), dependencies.getFilter(), filter);
        }
    }
}

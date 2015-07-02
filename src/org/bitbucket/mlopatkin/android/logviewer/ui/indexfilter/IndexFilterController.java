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

package org.bitbucket.mlopatkin.android.logviewer.ui.indexfilter;

import com.google.common.base.Predicate;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexframe.AbstractIndexController;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexframe.DaggerIndexFrameComponent;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexframe.IndexFrame;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexframe.IndexFrameComponent;
import org.bitbucket.mlopatkin.android.logviewer.ui.indexframe.IndexFrameModule;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogModelFilter;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogTable;
import org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.MainFrameDependencies;

import javax.inject.Inject;

public class IndexFilterController extends AbstractIndexController {

    private final IndexFrame frame;
    private final IndexFilterCollection owner;
    private final Predicate<LogRecord> filter;

    IndexFilterController(IndexFilterCollection owner, MainFrameDependencies dependencies,
                          LogTable mainTable,
                          LogModelFilter mainFilter,
                          Predicate<LogRecord> filter) {
        super(mainTable);
        this.owner = owner;
        this.filter = filter;
        IndexFrameComponent component =
                DaggerIndexFrameComponent.builder()
                                         .mainFrameDependencies(dependencies)
                                         .indexFrameModule(
                                                 new IndexFrameModule(this, null,
                                                                      new IndexFilter(mainFilter, filter)))
                                         .build();

        frame = component.createFrame();
    }

    public void setEnabled(boolean enabled) {
        if (enabled != frame.isVisible()) {
            frame.setVisible(enabled);
        }
    }

    public void destroy() {
        frame.dispose();
    }

    @Override
    public void onWindowClosed() {
        owner.onFilterDisabledByItself(filter);
    }

    public static class Factory {

        private final MainFrameDependencies dependencies;

        @Inject
        public Factory(MainFrameDependencies dependencies) {
            this.dependencies = dependencies;
        }

        public IndexFilterController create(IndexFilterCollection owner, Predicate<LogRecord> filter) {
            // TODO dependency cycle, dangerous
            // MainFilterController -> IndexFilterCollection -> Factory -> MainFrameDependencies -> MainFilterController
            // We probably can break this by factoring Filter out of MainFilterController and making both Factory and
            // MainFilterController to depend on this new Filter
            IndexFilterController controller =
                    new IndexFilterController(owner, dependencies, dependencies.getLogTable(), dependencies.getFilter(),
                                              filter);
            controller.setEnabled(true);
            return controller;
        }
    }
}

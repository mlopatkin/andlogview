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

import name.mlopatkin.andlogview.liblogcat.LogRecord;
import name.mlopatkin.andlogview.ui.indexframe.AbstractIndexController;
import name.mlopatkin.andlogview.ui.indexframe.DaggerIndexFrameDi_IndexFrameComponent;
import name.mlopatkin.andlogview.ui.indexframe.IndexFrame;
import name.mlopatkin.andlogview.ui.indexframe.IndexFrameDi;
import name.mlopatkin.andlogview.ui.logtable.LogModelFilter;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameDependencies;

import java.awt.EventQueue;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.swing.JTable;

public class IndexFilterController extends AbstractIndexController {
    private final IndexFrame frame;
    private final IndexFilterCollection owner;
    private final Predicate<LogRecord> filter;

    private boolean enabled = true;

    IndexFilterController(IndexFilterCollection owner, MainFrameDependencies dependencies, JTable mainTable,
            LogModelFilter mainFilter, Predicate<LogRecord> filter) {
        super(mainTable);
        this.owner = owner;
        this.filter = filter;
        IndexFrameDi.IndexFrameComponent component = DaggerIndexFrameDi_IndexFrameComponent.builder()
                .mainFrameDependencies(dependencies)
                .setIndexController(this)
                .setIndexFilter(new IndexFilter(mainFilter, filter))
                .build();
        frame = component.createFrame();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        // Postpone actual visibility change for two reasons:
        // 1. Main Frame may not be ready at this point (e.g. when restoring filters at startup).
        // 2. Client code may want to immediately disable the filter, post-ing avoids flickering.
        EventQueue.invokeLater(() -> {
            if (this.enabled != frame.isVisible()) {
                frame.setVisible(this.enabled);
            }
        });
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
            IndexFilterController controller = new IndexFilterController(
                    owner, dependencies, dependencies.getLogTable(), dependencies.getFilter(), filter);
            controller.setEnabled(true);
            return controller;
        }
    }
}

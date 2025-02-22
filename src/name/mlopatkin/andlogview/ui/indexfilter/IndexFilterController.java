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

import name.mlopatkin.andlogview.features.Features;
import name.mlopatkin.andlogview.filters.CompoundFilterModel;
import name.mlopatkin.andlogview.filters.MutableFilterModel;
import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.ui.filterdialog.IndexWindowFilter;
import name.mlopatkin.andlogview.ui.indexframe.AbstractIndexController;
import name.mlopatkin.andlogview.ui.indexframe.DaggerIndexFrameDi_IndexFrameComponent;
import name.mlopatkin.andlogview.ui.indexframe.IndexFrame;
import name.mlopatkin.andlogview.ui.indexframe.IndexFrameDi;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableModel;
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;
import name.mlopatkin.andlogview.utils.CommonChars;
import name.mlopatkin.andlogview.utils.MyStringUtils;
import name.mlopatkin.andlogview.utils.TextUtils;

import com.google.common.base.Joiner;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;

import java.awt.EventQueue;
import java.util.List;
import java.util.Objects;

import javax.inject.Named;
import javax.swing.JTable;

public class IndexFilterController extends AbstractIndexController implements AutoCloseable {
    private static final int MAX_TITLE_FILTER_DESC_LENGTH = 100;
    private static final Joiner commaJoiner = Joiner.on(", ");

    private final MutableFilterModel filterModel;
    private final IndexWindowFilter filter;
    private final IndexFrame frame;
    private final IndexFilter logModelFilter;

    @AssistedInject
    IndexFilterController(
            LogRecordTableModel logModel,
            DialogFactory dialogFactory,
            MutableFilterModel parentFilterModel,
            @Named(FOR_MAIN_FRAME) JTable mainTable,
            Features features,
            @Assisted IndexWindowFilter filter) {
        super(mainTable);
        this.filterModel = parentFilterModel;

        this.filter = filter;
        var parent = !features.useFilterTree.isEnabled()
                ? parentFilterModel
                : Objects.requireNonNull(parentFilterModel.findSubModel(filter));
        logModelFilter = new IndexFilter(
                new CompoundFilterModel(parent, filter));

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

    @Override
    public String getTitle() {
        var name = filter.getName();
        if (name != null) {
            return name;
        }

        StringBuilder title = new StringBuilder();
        var data = filter.getData();

        appendList(title, "tag", "tags", data.getTags());
        appendList(title, "PID", "PIDs", data.getPids());
        appendList(title, "app", "apps", data.getApps());

        var msg = data.getMessagePattern();
        if (msg != null && !msg.isEmpty()) {
            withSpace(title).append("message `").append(msg).append('`');
        }

        var prio = data.getPriority();
        if (prio != null && prio != LogRecord.Priority.LOWEST) {
            withSpace(title).append("priority>=").append(prio.getLetter());
        }

        if (title.length() == 0) {
            return "Index";
        }

        return "Index: " + MyStringUtils.abbreviateMiddle(title, CommonChars.ELLIPSIS, MAX_TITLE_FILTER_DESC_LENGTH,
                MAX_TITLE_FILTER_DESC_LENGTH / 2);
    }

    private StringBuilder withSpace(StringBuilder title) {
        if (title.length() > 0) {
            title.append("; ");
        }
        return title;
    }

    private void appendList(StringBuilder title, String captionSingle, String captionMultiple, List<?> elements) {
        if (!elements.isEmpty()) {
            withSpace(title).append(TextUtils.plural(elements.size(), captionSingle, captionMultiple)).append(' ');
            commaJoiner.appendTo(title, elements);
        }
    }

    @AssistedFactory
    public interface Factory {
        IndexFilterController create(IndexWindowFilter filter);
    }
}

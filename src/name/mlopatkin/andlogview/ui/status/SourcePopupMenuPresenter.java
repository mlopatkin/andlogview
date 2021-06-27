/*
 * Copyright 2021 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.ui.status;

import name.mlopatkin.andlogview.DataSourceHolder;
import name.mlopatkin.andlogview.liblogcat.DataSource;
import name.mlopatkin.andlogview.liblogcat.SourceMetadataItem;
import name.mlopatkin.andlogview.ui.GlobalClipboard;
import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;

import javax.inject.Inject;

@MainFrameScoped
class SourcePopupMenuPresenter {

    private final GlobalClipboard clipboard;
    private final DataSourceHolder dataSourceHolder;

    @Inject
    public SourcePopupMenuPresenter(GlobalClipboard clipboard, DataSourceHolder dataSourceHolder) {
        this.clipboard = clipboard;
        this.dataSourceHolder = dataSourceHolder;
    }

    public void showPopupMenuIfNeeded(SourceStatusPopupMenuView popupMenuView) {
        DataSource currentSource = dataSourceHolder.getDataSource();
        if (currentSource == null) {
            return;
        }

        boolean hasMetadataToCopy = false;
        for (SourceMetadataItem metadataItem : currentSource.getMetadata().getMetadataItems()) {
            popupMenuView.addCopyAction(metadataItem.getDisplayName(), metadataItem.getValue())
                    .addObserver(() -> clipboard.setText(metadataItem.getValue()));
            hasMetadataToCopy = true;
        }
        if (hasMetadataToCopy) {
            popupMenuView.show();
        }
    }
}

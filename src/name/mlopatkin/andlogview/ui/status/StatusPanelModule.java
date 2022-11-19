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

import name.mlopatkin.andlogview.ui.mainframe.MainFrameScoped;

import dagger.Module;
import dagger.Provides;

/**
 * Bindings for the Status Panel UI.
 */
@Module
public class StatusPanelModule {

    @Provides
    @MainFrameScoped
    static SourceStatusPresenter.View sourceStatusView(StatusPanelUi statusPanel) {
        return new SourceStatusViewImpl(statusPanel.sourceStatusLabel);
    }

    @Provides
    @MainFrameScoped
    static SearchStatusPresenter.View searchStatusView(StatusPanelUi statusPanel) {
        return new SearchStatusPresenter.View() {
            @Override
            public void showSearchMessage(String message) {
                statusPanel.searchStatusLabel.setVisible(true);
                statusPanel.searchStatusLabel.setText(message);
            }

            @Override
            public void hideSearchMessage() {
                statusPanel.searchStatusLabel.setVisible(false);
            }
        };
    }
}

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

import javax.inject.Inject;

/**
 * The Status Panel UI component.
 */
@MainFrameScoped
public class StatusPanel extends StatusPanelUi {
    @Inject
    public StatusPanel() {
    }

    public SourceStatusPresenter.View getSourceStatusView() {
        return new SourceStatusViewImpl(sourceStatusLabel);
    }

    public SearchStatusPresenter.View getSearchStatusView() {
        return new SearchStatusPresenter.View() {
            @Override
            public void showSearchMessage(String message) {
                searchStatusLabel.setVisible(true);
                searchStatusLabel.setText(message);
            }

            @Override
            public void hideSearchMessage() {
                searchStatusLabel.setVisible(false);
            }
        };
    }
}

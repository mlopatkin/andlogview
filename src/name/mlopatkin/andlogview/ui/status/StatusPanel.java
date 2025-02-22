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
import javax.swing.JPanel;

/**
 * The Status Panel UI component.
 */
@MainFrameScoped
public class StatusPanel {
    private final StatusPanelUi statusPanelUi;

    @Inject
    StatusPanel(
            StatusPanelUi statusPanelUi,
            @SuppressWarnings("unused") SourceStatusPresenter sourceStatusPresenter) {
        // Unused parameters are to bootstrap presenters as otherwise there's nobody to create them.
        this.statusPanelUi = statusPanelUi;
    }

    public JPanel getPanel() {
        return statusPanelUi.getPanel();
    }
}

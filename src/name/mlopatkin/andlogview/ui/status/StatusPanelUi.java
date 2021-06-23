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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

class StatusPanelUi {
    protected final JPanel statusPanel;
    protected final JLabel searchStatusLabel;
    protected final JLabel sourceStatusLabel;

    public StatusPanelUi() {
        statusPanel = new JPanel();
        statusPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));

        searchStatusLabel = new JLabel();
        searchStatusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(searchStatusLabel);

        Component horizontalGlue = Box.createHorizontalGlue();
        statusPanel.add(horizontalGlue);

        sourceStatusLabel = new JLabel();
        statusPanel.add(sourceStatusLabel);

        Component rigidArea = Box.createRigidArea(new Dimension(5, 16));
        statusPanel.add(rigidArea);
    }

    public JPanel getPanel() {
        return statusPanel;
    }
}

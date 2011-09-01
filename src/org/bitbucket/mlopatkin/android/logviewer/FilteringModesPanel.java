/*
 * Copyright 2011 Mikhail Lopatkin
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
package org.bitbucket.mlopatkin.android.logviewer;

import java.util.EnumMap;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class FilteringModesPanel extends JPanel implements ChangeListener {
    private ButtonGroup buttonGroup = new ButtonGroup();
    private EnumMap<FilteringMode, JRadioButton> buttons = new EnumMap<FilteringMode, JRadioButton>(
            FilteringMode.class);

    interface ModeChangedListener {
        void modeSelected(FilteringMode mode);
    }

    private ModeChangedListener listener;

    public FilteringModesPanel() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        for (FilteringMode mode : FilteringMode.values()) {
            addRadioButton(mode);
        }

        setSelectedMode(FilteringMode.getDefaultMode());
    }

    private void addRadioButton(FilteringMode mode) {
        JRadioButton button = new JRadioButton(mode.getDescription());
        buttonGroup.add(button);
        add(button);
        buttons.put(mode, button);
        button.addChangeListener(this);
    }

    public FilteringMode getSelectedMode() {
        for (Entry<FilteringMode, JRadioButton> entry : buttons.entrySet()) {
            if (entry.getValue().isSelected()) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("No filtering mode selected");
    }

    public void setSelectedMode(FilteringMode mode) {
        assert mode != null;
        buttons.get(mode).setSelected(true);
        notifyListener(mode);
    }

    public void setModeChangedListener(ModeChangedListener listener) {
        this.listener = listener;
    }

    private void notifyListener(FilteringMode mode) {
        if (listener != null) {
            listener.modeSelected(mode);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (((JRadioButton) e.getSource()).isSelected()) {
            notifyListener(getSelectedMode());
        }
    }
}

/*
 * Copyright 2014 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog;

import org.bitbucket.mlopatkin.android.logviewer.config.Configuration;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import java.awt.Color;

class ColorsComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {
    // TODO having string here is kind of lame
    private Color selected;

    @Override
    public Color getSelectedItem() {
        return selected;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selected = (Color) anItem;
    }

    @Override
    public String getElementAt(int index) {
        return "<html><span style='background-color: "
                + toString(Configuration.ui.highlightColors().get(index)) + "'>Color " + index
                + "</span></html>";

    }

    private String toString(Color color) {
        return String.format("#%06x", color.getRGB() & 0x00FFFFFF);
    }

    @Override
    public int getSize() {
        return Configuration.ui.highlightColors().size();
    }
}

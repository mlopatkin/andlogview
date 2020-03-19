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

import com.google.common.collect.Lists;

import org.bitbucket.mlopatkin.android.logviewer.config.Configuration;

import java.awt.Color;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

class ColorsComboBoxModel
        extends AbstractListModel<ColorsComboBoxModel.Item> implements ComboBoxModel<ColorsComboBoxModel.Item> {
    // TODO having string here is kind of lame
    private Item selected;
    private List<Item> items;

    public ColorsComboBoxModel() {
        items = Lists.newArrayListWithCapacity(Configuration.ui.highlightColors().size());
        int index = 0;
        for (Color color : Configuration.ui.highlightColors()) {
            items.add(new Item(color, index++));
        }
    }

    @Override
    public Item getSelectedItem() {
        return selected;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selected = ((Item) anItem);
    }

    @Override
    public Item getElementAt(int index) {
        return items.get(index);
    }

    @Override
    public int getSize() {
        return items.size();
    }

    public static class Item {
        private final Color color;
        private final int index;

        private Item(Color color, int index) {
            this.color = color;
            this.index = index;
        }

        @Override
        public String toString() {
            return String.format("<html><span style='background-color: #%06x '>Color %d</span></html>",
                    color.getRGB() & 0x00FFFFFF, index);
        }
    }
}

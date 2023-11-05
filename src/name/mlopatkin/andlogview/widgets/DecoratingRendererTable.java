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
package name.mlopatkin.andlogview.widgets;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Specialized version of JTable which allows wrapping of its cell renderers to
 * enrich display, e.g. color rows or highlight them.
 */
public class DecoratingRendererTable extends JTable {
    private static final long serialVersionUID = 2582491704714327034L;

    private final List<DecoratingCellRenderer> decorators = new ArrayList<>();

    public void addDecorator(DecoratingCellRenderer renderer) {
        decorators.add(renderer);
        revalidate();
        repaint();
    }

    public void removeDecorator(DecoratingCellRenderer renderer) {
        decorators.remove(renderer);
        revalidate();
        repaint();
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        // wrap main renderer with decorators
        TableCellRenderer result = super.getCellRenderer(row, column);
        for (DecoratingCellRenderer decorator : decorators) {
            decorator.setInnerRenderer(result);
            result = decorator;
        }
        return result;
    }
}

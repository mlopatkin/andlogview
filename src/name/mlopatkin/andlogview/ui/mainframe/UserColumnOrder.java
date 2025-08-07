/*
 * Copyright 2018 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.ui.mainframe;

import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.ColumnOrder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

/**
 * Order of the columns in the table. It is somewhat independent of what columns are available in the data source.
 * <p/>
 * This order is stored in the configuration file.
 */
public class UserColumnOrder implements ColumnOrder {
    private final List<Column> customizableOrder;
    private final Runnable changeCommitter;

    UserColumnOrder(Iterable<Column> columnOrder, Runnable changeCommitter) {
        customizableOrder = Lists.newArrayList(columnOrder);
        this.changeCommitter = changeCommitter;
    }

    public void setColumnBefore(Column movingColumn, @Nullable Column baseColumn) {
        Preconditions.checkArgument(movingColumn != Column.INDEX, "Index column isn't movable");
        Preconditions.checkArgument(baseColumn != Column.INDEX, "Can't insert column before index column");
        Preconditions.checkArgument(baseColumn != movingColumn, "Can't insert column before itself");

        customizableOrder.remove(movingColumn);
        if (baseColumn != null) {
            customizableOrder.add(customizableOrder.indexOf(baseColumn), movingColumn);
        } else {
            customizableOrder.add(movingColumn);
        }
        changeCommitter.run();
    }

    @Override
    public int compare(Column o1, Column o2) {
        return Integer.compare(customizableOrder.indexOf(o1), customizableOrder.indexOf(o2));
    }

    @Override
    public Iterator<Column> iterator() {
        return customizableOrder.iterator();
    }
}

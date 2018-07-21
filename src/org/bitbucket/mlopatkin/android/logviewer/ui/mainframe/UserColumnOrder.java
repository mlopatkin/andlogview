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

package org.bitbucket.mlopatkin.android.logviewer.ui.mainframe;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.bitbucket.mlopatkin.android.logviewer.config.ConfigStorage;
import org.bitbucket.mlopatkin.android.logviewer.config.ConfigStorage.ConfigStorageClient;
import org.bitbucket.mlopatkin.android.logviewer.config.ConfigStorage.InvalidJsonContentException;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.ColumnOrder;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Order of the columns in the table. It is somewhat independent of what columns are available in the data source.
 * <p/>
 * This order is stored in the configuration file.
 */
public class UserColumnOrder implements ColumnOrder {

    private final List<Column> customizableOrder;
    private final Runnable changeCommitter;

    @VisibleForTesting
    UserColumnOrder(Iterable<Column> columnOrder, Runnable changeCommitter) {
        customizableOrder = Lists.newArrayList(columnOrder);
        this.changeCommitter = changeCommitter;
    }

    UserColumnOrder(ConfigStorage storage) {
        customizableOrder = storage.loadConfig(CLIENT);
        changeCommitter = () -> storage.saveConfig(CLIENT, customizableOrder);
    }

    public void setColumnBefore(@Nonnull Column movingColumn, @Nullable Column baseColumn) {
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
    @Nonnull
    public Iterator<Column> iterator() {
        return customizableOrder.iterator();
    }

    @VisibleForTesting
    static final ConfigStorageClient<List<Column>> CLIENT = new ConfigStorageClient<List<Column>>() {
        @Override
        public String getName() {
            return "columnOrder";
        }

        @Override
        public List<Column> fromJson(Gson gson, JsonElement element)
                throws InvalidJsonContentException {
            List<Column> columns = Lists.newArrayList(gson.fromJson(element, Column[].class));
            ImmutableSet<Column> uniqueColumns = ImmutableSet.copyOf(columns);
            if (uniqueColumns.size() != columns.size()) {
                throw new InvalidJsonContentException("Duplicate columns in preference");
            }
            if (uniqueColumns.size() != Column.values().length) {
                throw new InvalidJsonContentException("Missing columns in preference");
            }
            return columns;
        }

        @Override
        public List<Column> getDefault() {
            return Lists.newArrayList(ColumnOrder.canonical());
        }

        @Override
        public JsonElement toJson(Gson gson, List<Column> value) {
            return gson.toJsonTree(value);
        }
    };
}

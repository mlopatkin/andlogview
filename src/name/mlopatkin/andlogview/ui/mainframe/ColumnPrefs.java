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

import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.ConfigStorageClient;
import name.mlopatkin.andlogview.config.InvalidJsonContentException;
import name.mlopatkin.andlogview.config.NamedClient;
import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.ColumnOrder;
import name.mlopatkin.andlogview.ui.logtable.ColumnTogglesModel;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Main frame's table columns preferences: visibility.
 */
class ColumnPrefs implements ColumnTogglesModel {
    private final ConfigStorage storage;
    private final ConfigStorageClient<ColumnPrefs> storageClient;

    private HashSet<Column> visibleColumns;
    private UserColumnOrder columnOrder;

    private ColumnPrefs(ConfigStorage storage, ConfigStorageClient<ColumnPrefs> storageClient) {
        this.storage = storage;
        this.storageClient = storageClient;
        visibleColumns =
                Sets.newHashSet(Column.TIME, Column.PID, Column.APP_NAME, Column.PRIORITY, Column.TAG, Column.MESSAGE);
        columnOrder = new UserColumnOrder(ColumnOrder.canonical(), this::commit);
    }

    private ColumnPrefs(ConfigStorage storage, ConfigStorageClient<ColumnPrefs> storageClient,
            @Nullable SerializableBase data) throws InvalidJsonContentException {
        this(storage, storageClient);
        checkJsonPrecondition(data != null, "Missing columns property");
        // NullAway cannot infer that data == null causes checkJsonPrecondition to throw.
        // noinspection ConstantConditions
        assert data != null;
        visibleColumns = new HashSet<>(checkVisibleColumns(data.visible));
        columnOrder = new UserColumnOrder(checkOrder(data.order), this::commit);
    }

    @Override
    public boolean isColumnAvailable(Column column) {
        return column != Column.INDEX;
    }

    @Override
    public boolean isColumnVisible(Column column) {
        return visibleColumns.contains(column);
    }

    @Override
    public void setColumnVisibility(Column column, boolean isVisible) {
        Preconditions.checkArgument(!isVisible || isColumnAvailable(column));
        Preconditions.checkArgument(isVisible || column != Column.MESSAGE);
        if (isVisible) {
            visibleColumns.add(column);
        } else {
            visibleColumns.remove(column);
        }

        commit();
    }

    public UserColumnOrder getColumnOrder() {
        return columnOrder;
    }

    private void commit() {
        storage.saveConfig(storageClient, this);
    }

    private static class SerializableBase {
        final Set<Column> visible;
        final List<Column> order;

        SerializableBase(Set<Column> visibleColumns, List<Column> columnOrder) {
            visible = visibleColumns;
            order = columnOrder;
        }
    }

    static class Factory extends NamedClient<ColumnPrefs> {
        private final ConfigStorage storage;

        @Inject
        Factory(ConfigStorage storage) {
            super("columns");
            this.storage = storage;
        }

        @Override
        public ColumnPrefs fromJson(Gson gson, JsonElement element) throws InvalidJsonContentException {
            SerializableBase data = gson.fromJson(element, SerializableBase.class);
            return new ColumnPrefs(storage, this, data);
        }

        @Override
        public ColumnPrefs getDefault() {
            return new ColumnPrefs(storage, this);
        }

        @Override
        public JsonElement toJson(Gson gson, ColumnPrefs value) {
            return gson.toJsonTree(new SerializableBase(value.visibleColumns, ImmutableList.copyOf(value.columnOrder)));
        }

        public ColumnPrefs loadFromConfig() {
            return storage.loadConfig(this);
        }
    }

    private static Set<Column> checkVisibleColumns(Set<Column> visibleColumns) throws InvalidJsonContentException {
        checkJsonPrecondition(visibleColumns != null, "Missing columns.visible property");
        checkJsonPrecondition(
                visibleColumns.contains(Column.MESSAGE), "The columns.visible must contain %s column", Column.MESSAGE);
        checkJsonPrecondition(
                !visibleColumns.contains(Column.INDEX), "The columns.visible must not contain %s column", Column.INDEX);
        return visibleColumns;
    }

    private static List<Column> checkOrder(List<Column> order) throws InvalidJsonContentException {
        checkJsonPrecondition(order != null, "Missing columns.order property");
        EnumSet<Column> allColumns = EnumSet.allOf(Column.class);
        checkJsonPrecondition(order.size() == allColumns.size() && allColumns.equals(EnumSet.copyOf(order)),
                "columns.order isn't a permutation of all colums");
        return order;
    }

    @FormatMethod
    private static void checkJsonPrecondition(boolean precondition, @FormatString String message, Object... args)
            throws InvalidJsonContentException {
        if (!precondition) {
            throw new InvalidJsonContentException(message, args);
        }
    }
}

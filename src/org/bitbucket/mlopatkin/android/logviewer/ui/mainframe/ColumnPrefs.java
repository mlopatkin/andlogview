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

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.bitbucket.mlopatkin.android.logviewer.config.ConfigStorage;
import org.bitbucket.mlopatkin.android.logviewer.config.ConfigStorage.ConfigStorageClient;
import org.bitbucket.mlopatkin.android.logviewer.config.ConfigStorage.InvalidJsonContentException;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.Column;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.ColumnTogglesModel;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Main frame's table columns preferences: visibility.
 */
class ColumnPrefs implements ColumnTogglesModel {
    private final ConfigStorage storage;
    private final ConfigStorageClient<ColumnPrefs> storageClient;

    private HashSet<Column> visibleColumns;

    private ColumnPrefs(ConfigStorage storage, ConfigStorageClient<ColumnPrefs> storageClient) {
        this.storage = storage;
        this.storageClient = storageClient;
        visibleColumns = Sets.newHashSet(
                Column.TIME,
                Column.PID,
                Column.APP_NAME,
                Column.PRIORITY,
                Column.TAG,
                Column.MESSAGE
        );
    }

    private ColumnPrefs(ConfigStorage storage, ConfigStorageClient<ColumnPrefs> storageClient,
            @Nullable SerializableBase data) throws InvalidJsonContentException {
        this(storage, storageClient);
        if (data == null || data.visible == null) {
            throw new InvalidJsonContentException("Missing columns.visible field");
        }
        if (!data.visible.contains(Column.MESSAGE)) {
            throw new InvalidJsonContentException("The columns.visible must contain %s column", Column.MESSAGE);
        }
        if (data.visible.contains(Column.INDEX)) {
            throw new InvalidJsonContentException("The columns.visible must not contain %s column", Column.INDEX);
        }
        visibleColumns = new HashSet<>(data.visible);
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

        storage.saveConfig(storageClient, this);
    }

    private static class SerializableBase {
        final Set<Column> visible;

        SerializableBase(Set<Column> visibleColumns) {
            visible = visibleColumns;
        }
    }

    static class Factory implements ConfigStorageClient<ColumnPrefs> {
        private final ConfigStorage storage;

        @Inject
        Factory(ConfigStorage storage) {
            this.storage = storage;
        }

        @Override
        public String getName() {
            return "columns";
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
            return gson.toJsonTree(new SerializableBase(value.visibleColumns));
        }

        public ColumnPrefs loadFromConfig() {
            return storage.loadConfig(this);
        }
    }
}

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

/**
 * Main frame's table columns preferences: visibility.
 */
class ColumnPrefs implements ColumnTogglesModel {
    private HashSet<Column> visible;

    ColumnPrefs() {
        visible = Sets.newHashSet(
                Column.TIME,
                Column.PID,
                Column.APP_NAME,
                Column.PRIORITY,
                Column.TAG,
                Column.MESSAGE
        );
    }

    private ColumnPrefs(@Nullable SerializableBase data) throws InvalidJsonContentException {
        if (data == null || data.visible == null) {
            throw new InvalidJsonContentException("Missing columns.visible field");
        }
        if (!data.visible.contains(Column.MESSAGE)) {
            throw new InvalidJsonContentException("The columns.visible must contain %s column", Column.MESSAGE);
        }
        if (data.visible.contains(Column.INDEX)) {
            throw new InvalidJsonContentException("The columns.visible must not contain %s column", Column.INDEX);
        }
        visible = new HashSet<>(data.visible);
    }

    @VisibleForTesting
    static final ConfigStorageClient<ColumnPrefs> CLIENT = new ConfigStorageClient<ColumnPrefs>() {
        @Override
        public String getName() {
            return "columns";
        }

        @Override
        public ColumnPrefs fromJson(Gson gson, JsonElement element) throws InvalidJsonContentException {
            SerializableBase data = gson.fromJson(element, SerializableBase.class);
            return new ColumnPrefs(data);
        }

        @Override
        public ColumnPrefs getDefault() {
            return new ColumnPrefs();
        }

        @Override
        public JsonElement toJson(Gson gson, ColumnPrefs value) {
            return gson.toJsonTree(value);
        }
    };

    @Override
    public boolean isColumnAvailable(Column column) {
        return column != Column.INDEX;
    }

    @Override
    public boolean isColumnVisible(Column column) {
        return visible.contains(column);
    }

    @Override
    public void setColumnVisibility(Column column, boolean isVisible) {
        Preconditions.checkArgument(!isVisible || isColumnAvailable(column));
        Preconditions.checkArgument(isVisible || column != Column.MESSAGE);
        if (isVisible) {
            visible.add(column);
        } else {
            visible.remove(column);
        }
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static class SerializableBase {
        Set<Column> visible;
    }

    public static ColumnPrefs loadFromConfig(ConfigStorage storage) {
        return storage.loadConfig(CLIENT);
    }

    public void saveToStorage(ConfigStorage storage) {
        storage.saveConfig(CLIENT, this);
    }
}

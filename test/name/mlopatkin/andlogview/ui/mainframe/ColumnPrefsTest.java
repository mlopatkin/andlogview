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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import name.mlopatkin.andlogview.config.ConfigStorage;
import name.mlopatkin.andlogview.config.FakeInMemoryConfigStorage;
import name.mlopatkin.andlogview.config.InvalidJsonContentException;
import name.mlopatkin.andlogview.test.AdaptingMatcher;
import name.mlopatkin.andlogview.ui.logtable.Column;
import name.mlopatkin.andlogview.ui.logtable.ColumnOrder;
import name.mlopatkin.andlogview.ui.logtable.TogglesModelTestUtils;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

public class ColumnPrefsTest {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private ConfigStorage storage = new FakeInMemoryConfigStorage();
    private ColumnPrefs.Factory factory = new ColumnPrefs.Factory(storage);

    @Test
    public void savedPrefsAreRestoredCorrectly() throws Exception {
        ColumnPrefs prefs = factory.getDefault();
        JsonElement jsonElement = factory.toJson(gson, prefs);
        ColumnPrefs deserializedPrefs = factory.fromJson(gson, jsonElement);

        assertThat(prefs,
                TogglesModelTestUtils.visibleColumns(
                        equalTo(TogglesModelTestUtils.getVisibleColumns(deserializedPrefs))));
        assertThat(prefs.getColumnOrder(), matchesOrder(ColumnOrder.canonical()));
    }

    @Test(expected = InvalidJsonContentException.class)
    public void nullJsonThrows() throws Exception {
        factory.fromJson(gson, toJsonElement(""));
    }

    @Test(expected = InvalidJsonContentException.class)
    public void emptyJsonThrows() throws Exception {
        factory.fromJson(gson, toJsonElement("{}"));
    }

    @Test(expected = InvalidJsonContentException.class)
    public void emptyVisibleListThrows() throws Exception {
        factory.fromJson(gson, buildVisibilityConfig());
    }

    @Test(expected = InvalidJsonContentException.class)
    public void missingMessageColumnThrows() throws Exception {
        factory.fromJson(gson, buildVisibilityConfig(Column.APP_NAME, Column.PID));
    }

    @Test
    public void havingOnlyMessageColumnIsValid() throws Exception {
        ColumnPrefs prefs = factory.fromJson(gson, buildVisibilityConfig(Column.MESSAGE));

        assertThat(prefs, TogglesModelTestUtils.visibleColumns(contains(Column.MESSAGE)));
    }

    @Test(expected = InvalidJsonContentException.class)
    public void havingIndexColumnIsNotValid() throws Exception {
        factory.fromJson(gson, buildVisibilityConfig(Column.MESSAGE, Column.INDEX));
    }

    @Test
    public void allParsedColumnsAreVisible() throws Exception {
        ColumnPrefs prefs = factory.fromJson(gson, buildVisibilityConfig(Column.MESSAGE, Column.APP_NAME, Column.TIME));

        assertThat(prefs,
                TogglesModelTestUtils.visibleColumns(containsInAnyOrder(Column.MESSAGE, Column.APP_NAME, Column.TIME)));
    }

    // Tests for default prefs
    @Test
    public void indexColumnIsNotAvailable() {
        ColumnPrefs prefs = factory.getDefault();

        assertFalse("Index column isn't available in MainFrame", prefs.isColumnAvailable(Column.INDEX));
    }

    @Test
    public void allButIndexColumnAreAvailable() {
        ColumnPrefs prefs = factory.getDefault();
        Set<Column> allButIndex = getAllColumnsButIndex();
        assertThat(TogglesModelTestUtils.getAvailableColumns(prefs), equalTo(allButIndex));
    }

    @Test
    public void defaultOrderIsCanonical() {
        ColumnPrefs prefs = factory.getDefault();

        assertThat(prefs.getColumnOrder(), matchesOrder(ColumnOrder.canonical()));
    }

    @Test
    public void makingColumnVisibleWorks() {
        ColumnPrefs prefs = factory.getDefault();
        Assume.assumeFalse(prefs.isColumnVisible(Column.TID));

        prefs.setColumnVisibility(Column.TID, true);

        assertTrue(prefs.isColumnVisible(Column.TID));
    }

    @Test
    public void makingColumnHiddenWorks() {
        ColumnPrefs prefs = factory.getDefault();
        Assume.assumeTrue(prefs.isColumnVisible(Column.PID));

        prefs.setColumnVisibility(Column.PID, false);

        assertFalse(prefs.isColumnVisible(Column.TID));
    }

    @Test
    public void messageColumnIsVisibleByDefault() {
        ColumnPrefs prefs = factory.getDefault();

        assertTrue(prefs.isColumnVisible(Column.MESSAGE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void messageColumnCannotBeHidden() {
        ColumnPrefs prefs = factory.getDefault();

        prefs.setColumnVisibility(Column.MESSAGE, false);
    }

    @Test
    public void indexColumnIsHiddenByDefault() {
        ColumnPrefs prefs = factory.getDefault();

        assertFalse(prefs.isColumnVisible(Column.INDEX));
    }

    @Test(expected = IllegalArgumentException.class)
    public void indexColumnCannotBeShown() {
        ColumnPrefs prefs = factory.getDefault();

        prefs.setColumnVisibility(Column.INDEX, true);
    }

    @Test
    public void indexColumnCanBeHidden() {
        ColumnPrefs prefs = factory.getDefault();

        prefs.setColumnVisibility(Column.INDEX, false);

        assertFalse(prefs.isColumnAvailable(Column.INDEX));
        assertFalse(prefs.isColumnVisible(Column.INDEX));
    }

    @Test
    public void changedVisibilityIsPersisted() {
        ColumnPrefs prefs = factory.loadFromConfig();
        Assume.assumeTrue(prefs.isColumnVisible(Column.APP_NAME));

        prefs.setColumnVisibility(Column.APP_NAME, false);

        ColumnPrefs reloadedPrefs = factory.loadFromConfig();

        assertFalse(
                "Expected hidden column to remain hidden after reload", reloadedPrefs.isColumnVisible(Column.APP_NAME));
    }

    @Test(expected = InvalidJsonContentException.class)
    public void missingOrderListThrows() throws InvalidJsonContentException {
        factory.fromJson(gson, buildConfig(getAllColumnsButIndex(), null));
    }

    @Test(expected = InvalidJsonContentException.class)
    public void emptyOrderListThrows() throws InvalidJsonContentException {
        factory.fromJson(gson, buildOrderConfig());
    }

    @Test
    public void canonicalOrderParsesSucessfully() throws InvalidJsonContentException {
        ColumnPrefs prefs = factory.fromJson(gson, buildOrderConfig(ColumnOrder.canonical()));

        assertThat(prefs.getColumnOrder(), matchesOrder(ColumnOrder.canonical()));
    }

    @Test
    public void shuffledOrderParsesSucessfully() throws InvalidJsonContentException {
        ImmutableList<Column> order = ImmutableList.of(Column.APP_NAME, Column.INDEX, Column.MESSAGE, Column.PID,
                Column.PRIORITY, Column.TAG, Column.TID, Column.TIME);
        Assume.assumeThat(order, Matchers.iterableWithSize(Column.values().length));

        ColumnPrefs prefs = factory.fromJson(gson, buildOrderConfig(order));

        assertThat(prefs.getColumnOrder(), matchesOrder(order));
    }

    @Test(expected = InvalidJsonContentException.class)
    public void missingColumnThrows() throws InvalidJsonContentException {
        // missing INDEX
        ImmutableList<Column> order = ImmutableList.of(
                Column.APP_NAME, Column.MESSAGE, Column.PID, Column.PRIORITY, Column.TAG, Column.TID, Column.TIME);
        Assume.assumeThat(order, Matchers.iterableWithSize(Column.values().length - 1));

        factory.fromJson(gson, buildOrderConfig(order));
    }

    @Test(expected = InvalidJsonContentException.class)
    public void duplicateColumnThrows() throws InvalidJsonContentException {
        // duplicate message
        ImmutableList<Column> order = ImmutableList.of(Column.APP_NAME, Column.INDEX, Column.MESSAGE, Column.PID,
                Column.PRIORITY, Column.TAG, Column.TID, Column.TIME, Column.MESSAGE);
        Assume.assumeThat(order, Matchers.iterableWithSize(Column.values().length + 1));

        factory.fromJson(gson, buildOrderConfig(order));
    }

    @Test(expected = InvalidJsonContentException.class)
    public void duplicateColumnInsteadOfMissingThrows() throws InvalidJsonContentException {
        // duplicate message, missing index
        ImmutableList<Column> order = ImmutableList.of(Column.APP_NAME, Column.MESSAGE, Column.MESSAGE, Column.PID,
                Column.PRIORITY, Column.TAG, Column.TID, Column.TIME);
        Assume.assumeThat(order, Matchers.iterableWithSize(Column.values().length));

        factory.fromJson(gson, buildOrderConfig(order));
    }

    // Helper methods
    private JsonElement buildVisibilityConfig(Column... visibleColumns) {
        return buildConfig(Arrays.asList(visibleColumns), ColumnOrder.canonical());
    }

    private JsonElement buildOrderConfig(Column... order) {
        return buildOrderConfig(Arrays.asList(order));
    }

    private JsonElement buildOrderConfig(Iterable<Column> order) {
        return buildConfig(getAllColumnsButIndex(), order);
    }

    private Set<Column> getAllColumnsButIndex() {
        return TogglesModelTestUtils.getMatchingColumns(c -> c != Column.INDEX);
    }

    private JsonElement buildConfig(@Nullable Iterable<Column> visibleColumns, @Nullable Iterable<Column> order) {
        JsonObject config = new JsonObject();
        if (visibleColumns != null) {
            config.add("visible", toJsonArray(visibleColumns));
        }
        if (order != null) {
            config.add("order", toJsonArray(order));
        }
        return config;
    }

    private JsonArray toJsonArray(Iterable<Column> visibleColumns) {
        JsonArray visibles = new JsonArray();
        for (Column c : visibleColumns) {
            visibles.add(c.name());
        }
        return visibles;
    }

    private JsonElement toJsonElement(String s) {
        return JsonParser.parseString(s);
    }

    private static Matcher<ColumnOrder> matchesOrder(ColumnOrder expectedOrder) {
        return new AdaptingMatcher<>(ImmutableList::copyOf, Matchers.equalTo(ImmutableList.copyOf(expectedOrder)));
    }

    private static Matcher<ColumnOrder> matchesOrder(Iterable<Column> expectedOrder) {
        return new AdaptingMatcher<>(ImmutableList::copyOf, Matchers.equalTo(ImmutableList.copyOf(expectedOrder)));
    }
}

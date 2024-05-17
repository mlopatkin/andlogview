/*
 * Copyright 2024 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.filtertree;

import static org.assertj.core.api.Assertions.assertThat;

import name.mlopatkin.andlogview.ui.filtertree.FakeFilterTreeModel.FakeFilter;

import com.google.common.collect.ImmutableList;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import javax.swing.tree.TreeNode;

class TreeModelAdapterTest {
    private static final ValueRenderer<FakeFilter> value = new ValueRenderer<>(FakeFilter.class);
    FakeFilterTreeModel filterModel = new FakeFilterTreeModel();

    @Test
    void createdModelContainsFilters() {
        filterModel.addFilter("filter1");

        var model = new TreeModelAdapter(filterModel);
        assertChildren(model.getRoot()).singleElement().satisfies(filter ->
                assertThatFilter(filter).hasText("filter1"));
    }

    @Test
    void disablingFilterUpdatesModel() {
        var filter1 = filterModel.addFilter("filter1");

        var model = new TreeModelAdapter(filterModel);
        filterModel.setFilterEnabled(filter1, false);

        assertChildren(model.getRoot()).singleElement().satisfies(filter ->
                assertThatFilter(filter).hasText("filter1").isDisabled());
    }

    @Test
    void disablingFilterDoesNotChangeNode() {
        var filter1 = filterModel.addFilter("filter1");

        var model = new TreeModelAdapter(filterModel);
        var childNode = model.getChild(model.getRoot(), 0);
        filterModel.setFilterEnabled(filter1, false);

        // This is important for tree selection to work properly.
        assertThat(model.getChild(model.getRoot(), 0)).isSameAs(childNode);
    }

    @Test
    void removingFilterUpdatesModel() {
        var filter1 = filterModel.addFilter("filter1");

        var model = new TreeModelAdapter(filterModel);
        filterModel.removeFilter(filter1);

        assertChildren(model.getRoot()).isEmpty();
    }

    private static AbstractListAssert<
            ?,
            List<? extends FakeFilter>,
            FakeFilter,
            ObjectAssert<FakeFilter>> assertChildren(TreeNode node) {
        return assertThat(children(node)).map(value::toModel);
    }

    @SuppressWarnings({"unchecked", "RedundantSuppression"})
    private static List<TreeNode> children(TreeNode node) {
        // Javac doesn't like Enumerable for some reason and emits several unchecked warnings here.
        return ImmutableList.copyOf(Collections.list(node.children()));
    }

    private static class FilterAssert extends AbstractAssert<FilterAssert, FakeFilter> {
        protected FilterAssert(FakeFilter fakeFilter) {
            super(fakeFilter, FilterAssert.class);
        }

        public FilterAssert hasText(String text) {
            assertThat(actual.getText()).isEqualTo(text);
            return this;
        }

        public FilterAssert isDisabled() {
            assertThat(actual.isEnabled()).isFalse();
            return this;
        }
    }

    private static FilterAssert assertThatFilter(FakeFilter f) {
        return new FilterAssert(f);
    }
}

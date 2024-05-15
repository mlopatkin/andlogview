/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.search;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class ListSearchModel<T> implements SearchDataModel<T, Integer> {
    private final List<T> items;

    public ListSearchModel(List<T> items) {
        this.items = ImmutableList.copyOf(items);
    }

    @Override
    public SearchCursor<T, Integer> newCursor() {
        return new Cursor();
    }

    private class Cursor extends AbstractIndexCursor<T, Integer> {
        @Override
        protected Integer indexToPosition(int index) {
            return index;
        }

        @Override
        protected int positionToIndex(Integer position) {
            return position;
        }

        @Override
        protected T getValueAtIndex(int index) {
            return items.get(index);
        }

        @Override
        protected int size() {
            return items.size();
        }
    }
}

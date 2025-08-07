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

package name.mlopatkin.andlogview.ui.search;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class FakeSearchableView implements SearchPresenter.SearchableView<Integer> {
    private @Nullable Integer selectedRow;

    public void setSelectedRow(int row) {
        selectedRow = row;
    }

    @Override
    public void showSearchResult(Integer row) {
        selectedRow = row;
    }

    @Override
    public Optional<Integer> getSearchStartPosition() {
        return Optional.ofNullable(selectedRow);
    }
}

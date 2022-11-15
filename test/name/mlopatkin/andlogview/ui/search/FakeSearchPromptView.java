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

public class FakeSearchPromptView extends AbstractSearchPromptView {
    private String searchPattern = "";

    @Override
    public void focus() {}

    @Override
    public void clearSearchPattern() {
        searchPattern = "";
    }

    @Override
    public void showPatternError(String errorMessage) {}

    public String getSearchPattern() {
        return searchPattern;
    }

    @Override
    public void commit(String searchPattern) {
        this.searchPattern = searchPattern;
        super.commit(searchPattern);
    }

    @Override
    public void discard() {
        super.discard();
    }
}

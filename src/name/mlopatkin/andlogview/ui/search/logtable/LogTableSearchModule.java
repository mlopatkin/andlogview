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

package name.mlopatkin.andlogview.ui.search.logtable;

import name.mlopatkin.andlogview.logmodel.LogRecord;
import name.mlopatkin.andlogview.search.SearchModel;
import name.mlopatkin.andlogview.search.logrecord.RowSearchStrategy;
import name.mlopatkin.andlogview.ui.search.SearchPatternCompiler;
import name.mlopatkin.andlogview.ui.search.SearchScoped;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class LogTableSearchModule {
    @Provides
    @SearchScoped
    static SearchModel<LogRecord, TablePosition, RowSearchStrategy> createSearchModel(
            LogTableSearchAdapter searchAdapter) {
        return new SearchModel<>(searchAdapter);
    }

    @Binds
    abstract SearchPatternCompiler<RowSearchStrategy> createPatternCompiler(LogRecordSearchPatternCompiler compiler);
}

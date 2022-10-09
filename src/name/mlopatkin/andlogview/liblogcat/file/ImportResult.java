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

package name.mlopatkin.andlogview.liblogcat.file;

import name.mlopatkin.andlogview.logmodel.DataSource;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a result of loading the data source. May carry information about problems along the actual source.
 */
public class ImportResult {
    private final DataSource dataSource;
    private final Collection<ImportProblem> problems;

    public ImportResult(DataSource dataSource) {
        this(dataSource, Collections.emptySet());
    }

    public ImportResult(DataSource dataSource, Collection<ImportProblem> problems) {
        this.dataSource = dataSource;
        this.problems = problems;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Collection<ImportProblem> getProblems() {
        return problems;
    }
}

/*
 * Copyright 2023 the Andlogview authors
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

package name.mlopatkin.andlogview.ui;

import name.mlopatkin.andlogview.logmodel.DataSource;
import name.mlopatkin.andlogview.utils.Cancellable;

/**
 * A pending {@link DataSource} being initialized, potentially with user interactions.
 */
public interface PendingDataSource extends Cancellable {
    /**
     * Converts existing cancellable into {@link PendingDataSource}.
     *
     * @param cancellable the cancellable to convert
     * @return the PendingDataSource that cancels the provided cancellable upon cancellation
     */
    static PendingDataSource fromCancellable(Cancellable cancellable) {
        if (cancellable instanceof PendingDataSource dataSourcePromise) {
            return dataSourcePromise;
        }
        return cancellable::cancel;
    }
}

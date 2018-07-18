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

package org.bitbucket.mlopatkin.android.logviewer.ui.logtable;

/**
 * Model for columns' visibility toggles.
 */
public interface ColumnTogglesModel {
    /**
     * Returns whether the column is available in the data. It should be displayed if visible. Not all available
     * columns can be hidden though, it is controlled with {@link Column#isToggleable()}.
     *
     * @param column the column to check availability for
     * @return {@code true} if the column is present in the data
     */
    boolean isColumnAvailable(Column column);

    /**
     * Returns whether the column is currently visible. If the column isn't available then it isn't visible also.
     *
     * @param column the column to check visibility for
     * @return {@code true} if the column should be visible
     */
    boolean isColumnVisible(Column column);

    /**
     * Changes the visibility of the column. If the visibility of the column matches {@code isVisible} then this
     * method is a no-op (in particular it doesn't throw exceptions).
     *
     * @param column the column to change visibility of
     * @param isVisible {@code true} if the column should be shown, {@code false} if the column should be hidden
     * @throws IllegalArgumentException if the column isn't toggleable or isn't available
     */
    void setColumnVisibility(Column column, boolean isVisible);
}

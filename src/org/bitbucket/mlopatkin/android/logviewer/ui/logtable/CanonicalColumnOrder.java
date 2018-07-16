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

import java.util.Arrays;
import java.util.Iterator;

/**
 * Non-customizable "natural" column order.
 */
public class CanonicalColumnOrder implements ColumnOrder {

    @Override
    public Iterator<Column> iterator() {
        return Arrays.asList(Column.values()).iterator();
    }

    @Override
    public int compare(Column o1, Column o2) {
        return o1.compareTo(o2);
    }
}

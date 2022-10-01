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

package name.mlopatkin.andlogview.logmodel;

import name.mlopatkin.andlogview.utils.events.EmptyObservable;
import name.mlopatkin.andlogview.utils.events.Observable;

class EmptyLogModel implements LogModel {
    @Override
    public int size() {
        return 0;
    }

    @Override
    public LogRecord getAt(int index) {
        throw new IndexOutOfBoundsException("Cannot get record at " + index + " because the model is empty");
    }

    @Override
    public void clear() {
    }

    @Override
    public Observable<Observer> asObservable() {
        return EmptyObservable.instance();
    }
}

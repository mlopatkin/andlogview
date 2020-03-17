/*
 * Copyright 2015 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.utils.events;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Clients use Observable to register and unregister observers.
 * <p/>
 * This class isn't thread-safe.
 *
 * @param <T> the type of the observers
 */
@NotThreadSafe
public interface Observable<T> {

    /**
     * Adds the observer if it wasn't added already. It is safe to add observers inside observer's callback but newly
     * added observers will not be notified until the next callback cycle.
     *
     * @param observer the non-null observer to add.
     */
    void addObserver(T observer);

    /**
     * Removes observer if it was registered. It the observer wasn't registered or was already removed or is null then
     * this is a no-op. It is safe to remove observers inside observer's callback and the changes are applied
     * immediately.
     *
     * @param observer the observer to remove
     */
    void removeObserver(T observer);
}

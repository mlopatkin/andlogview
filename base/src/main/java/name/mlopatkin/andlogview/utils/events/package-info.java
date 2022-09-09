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

/**
 * I really hate writing Observer pattern again and again. Here is my attempt to generalize it.
 * <p/>
 * The idea is to provide generic Observable interface that may be exposed to clients. Clients will register/unregister
 * from this thing. The provider of this Observable will have another view (Subject) of the thing that allows to
 * walk through all registered clients and notify them.
 */
package name.mlopatkin.andlogview.utils.events;

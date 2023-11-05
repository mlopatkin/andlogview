/*
 * Copyright 2014 Mikhail Lopatkin
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
 * The almighty log-displaying table. This table is the core of the application.
 * It is capable of displaying and filtering log entries, autoscrolling and alike.
 * This package groups together all related classes.
 * <p>
 * Clients are expected to have a @{@link name.mlopatkin.andlogview.ui.logtable.LogTableScoped} Dagger 2
 * {@link dagger.Component} to assemble the table.
 */
package name.mlopatkin.andlogview.ui.logtable;

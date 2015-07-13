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
 * The bottom panel that can be used to create/delete/toggle filters.
 * <p/>
 * Embedder pushes data via an instance of the
 * {@link org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.FilterPanelModel}.
 * <p/>
 * Overall architectucture of the panel is quite complicated. FilterPanel holds buttons. Each button corresponds to the
 * instance of the {@link org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.PanelFilterView}. Toggling button
 * or selecting context menu action is translated to a call to FilterPanelModel. FilterPanelModel maintains a mapping
 * of PanelFilterViews to {@link org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.PanelFilter}s. Each method
 * call translate to call to PanelFilter's method. PanelFilter is provided by the embedder and it will change internal
 * state of the filter model. Then embedder will then change state of the FilterPanelModel via its public method.
 */
package org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel;

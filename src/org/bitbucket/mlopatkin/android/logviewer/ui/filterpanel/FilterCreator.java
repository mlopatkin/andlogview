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

package org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel;

/**
 * A facility to create filters with dialog. User can open dialog by clicking "plus" button or double-clicking filter
 * panel.
 */
public interface FilterCreator {
    /**
     * Opens a filter creation dialog. Embedder expected to add {@link PanelFilter} to {@link FilterPanelModel} when
     * dialog is completed successfully.
     */
    void createFilterWithDialog();
}

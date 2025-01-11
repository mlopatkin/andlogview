/*
 * Copyright 2011 Mikhail Lopatkin
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
package name.mlopatkin.andlogview.ui.indexframe;

/**
 * The instance of the IndexController translates actions with {@link IndexFrame} to the main window.
 */
public interface IndexController {
    /**
     * A title of the index window
     *
     * @return the title of the index window
     */
    String getTitle();

    /**
     * Called when a row in the table must be activated (scrolled to and selected).
     *
     * @param row the model index of the row
     */
    void activateRow(int row);

    /**
     * Called when the IndexFrame is closed.
     */
    void onWindowClosed();
}

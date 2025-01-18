/*
 * Copyright 2025 the Andlogview authors
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

import name.mlopatkin.andlogview.base.AppResources;

import com.formdev.flatlaf.extras.FlatSVGUtils;

import javax.swing.JFrame;

public class AppFrame extends JFrame {
    public AppFrame() {
        initialize();
    }

    public AppFrame(String title) {
        super(title);

        initialize();
    }

    private void initialize() {
        setIconImages(FlatSVGUtils.createWindowIconImages(AppResources.getUrl("andlogview.svg")));
    }
}

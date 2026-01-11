/*
 * Copyright 2026 the Andlogview authors
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

package name.mlopatkin.andlogview.widgets;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;

/**
 * A panel that gets its border from a custom resource. It automatically picks up changes in the UIManager when L&amp;F
 * changes.
 */
public class BorderPanel extends JPanel {
    private final ClientProperty<Border> borderSource;

    public BorderPanel(ClientProperty<Border> borderSource) {
        this.borderSource = borderSource;
        setBorder(borderSource.getDefault());
    }

    @Override
    // This method may be called before our constructor finishes and sees partially initialized value.
    @SuppressWarnings("ConstantValue")
    public void updateUI() {
        super.updateUI();

        var isCalledFromParentConstructor = borderSource == null;
        if (!isCalledFromParentConstructor && (getBorder() == null || getBorder() instanceof UIResource)) {
            setBorder(borderSource.get(this));
        }
    }
}

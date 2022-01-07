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

package name.mlopatkin.andlogview.widgets;

import com.formdev.flatlaf.ui.FlatBorder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;

/**
 * This is subclass of the FlatLaf default border that allows to configure how border is drawn when the component is
 * focused, including on a per-component basis with Swing's client properties. The borders have no state so one instance
 * can be used for multiple components freely. The {@linkplain ClientProperty} fields can be used to configure
 * per-border defaults, for example one can define a border for all Tables without the need to configure client
 * properties of each table.
 */
public class ConfigurableFlatBorder extends FlatBorder {
    public final ClientProperty<Integer> focusWidthProp = ClientProperty.intProperty();
    public final ClientProperty<Float> innerFocusWidthProp = ClientProperty.floatProperty();
    public final ClientProperty<Color> focusedBorderColorProp = ClientProperty.colorProperty();

    @Override
    protected int getFocusWidth(Component c) {
        return focusWidthProp.getOrElse(c, super.getFocusWidth(c));
    }

    @Override
    protected float getInnerFocusWidth(Component c) {
        return innerFocusWidthProp.getOrElse(c, super.getInnerFocusWidth(c));
    }

    @Override
    protected Paint getBorderColor(Component c) {
        Paint baseBorderColor = super.getBorderColor(c);
        if (isEnabled(c) && isFocused(c)) {
            return ClientProperty.getWithUpcast(focusedBorderColorProp, c, baseBorderColor);
        }
        return baseBorderColor;
    }
}

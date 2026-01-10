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

package name.mlopatkin.andlogview.ui.themes;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.IntelliJTheme;
import com.google.common.io.Resources;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * FlatLaf L&amp;F with Light Flat IDEA theme.
 */
class FlatLafTheme implements Theme {
    static final Theme LIGHT = new FlatLafTheme("Light", FlatLafTheme::createLight);
    static final Theme DARK = new FlatLafTheme("Dark", FlatLafTheme::createDark);

    private final String displayName;
    private final Supplier<FlatLaf> lafBuilder;

    static {
        FlatLaf.registerCustomDefaultsSource(FlatLafTheme.class.getPackageName());
    }

    private FlatLafTheme(String displayName, Supplier<FlatLaf> lafBuilder) {
        this.displayName = displayName;
        this.lafBuilder = lafBuilder;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void install() {
        if (!FlatLaf.setup(lafBuilder.get())) {
            failure(displayName, null);
        }
    }

    @Override
    public ThemedWidgetFactory getWidgetFactory() {
        return new FlatLafWidgetFactory();
    }

    private static FlatLaf createLight() throws ThemeException {
        try {
            return IntelliJTheme.createLaf(Resources.asByteSource(FlatLafThemes.LIGHTFLAT.getUrl()).openStream());
        } catch (IOException e) {
            throw failure("Light", e);
        }
    }

    private static FlatLaf createDark() {
        return new FlatDarkLaf();
    }

    private static ThemeException failure(String theme, @Nullable Throwable exception) {
        throw new ThemeException("Failed to load %s theme".formatted(theme), exception);
    }

    @Override
    public String toString() {
        return "FlatLafTheme(" + getDisplayName() + ")";
    }
}

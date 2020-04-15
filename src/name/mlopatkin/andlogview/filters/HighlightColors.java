/*
 * Copyright 2020 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.filters;

import com.google.common.collect.ImmutableList;

import org.bitbucket.mlopatkin.android.logviewer.config.Configuration;

import java.awt.Color;

import javax.inject.Inject;

/**
 * A list of default highlight colors.
 */
public class HighlightColors {
    @Inject
    public HighlightColors() {
    }

    public ImmutableList<Color> getColors() {
        return ImmutableList.copyOf(Configuration.ui.highlightColors());
    }
}

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

package name.mlopatkin.andlogview.widgets.dialogs;

import javax.swing.JComponent;

/**
 * An error dialog, with optional expandable detailed stack trace.
 */
public interface ErrorOptionPaneBuilder extends OptionPaneBuilder {
    /**
     * Adds expandable detailed stack trace of the failure.
     *
     * @param failure the failure
     * @return this
     */
    ErrorOptionPaneBuilder details(Throwable failure);

    /**
     * {@inheritDoc}
     */
    @Override
    ErrorOptionPaneBuilder message(String message);

    /**
     * {@inheritDoc}
     */
    @Override
    ErrorOptionPaneBuilder messageContent(JComponent messageContent);

    /**
     * {@inheritDoc}
     */
    @Override
    ErrorOptionPaneBuilder extraMessage(Object message);

    /**
     * {@inheritDoc}
     */
    @Override
    ErrorOptionPaneBuilder addInitialOption(String title, Runnable action);

    /**
     * {@inheritDoc}
     */
    @Override
    ErrorOptionPaneBuilder addOption(String title, Runnable action);

    /**
     * {@inheritDoc}
     */
    @Override
    ErrorOptionPaneBuilder addCancelOption(String title, Runnable action);

    /**
     * {@inheritDoc}
     */
    @Override
    ErrorOptionPaneBuilder addCancelOptionAsInitial(String title, Runnable action);
}

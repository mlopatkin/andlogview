/*
 * Copyright 2024 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.mainframe;

import name.mlopatkin.andlogview.thirdparty.observerlist.ObserverList;
import name.mlopatkin.andlogview.utils.events.ScopedObserver;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.inject.Inject;

/**
 * Handles actions when the main frame closes.
 */
@MainFrameScoped
public class MainFrameCleaner {
    private final ObserverList<Runnable> cleanupActions = new ObserverList<>();

    @Inject
    MainFrameCleaner() {}

    @Inject
    void registerAsListener(MainFrameUi mainFrameUi) {
        mainFrameUi.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                for (var action : cleanupActions) {
                    action.run();
                }
            }
        });
    }

    /**
     * Adds a cleanup action to run when the main frame closes.
     *
     * @param cleanupAction the action
     * @return the handler to remove the action
     */
    @CanIgnoreReturnValue
    public ScopedObserver addAction(Runnable cleanupAction) {
        cleanupActions.addObserver(cleanupAction);
        return () -> cleanupActions.removeObserver(cleanupAction);
    }
}

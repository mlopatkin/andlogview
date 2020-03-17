/*
 * Copyright 2018 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This class manages actions that has to be performed during shutdown of the app.
 */
@Singleton
public class AtExitManager {
    private static final Logger logger = Logger.getLogger(AtExitManager.class);

    private final List<Runnable> actions = new ArrayList<>();
    private final Thread atExitWorker = new Thread(this::onExit, "AtExitWorker");

    @Inject
    public AtExitManager() {
    }

    public void registerExitAction(Runnable action) {
        if (actions.isEmpty()) {
            addHook();
        }
        actions.add(action);
    }

    private void addHook() {
        Runtime.getRuntime().addShutdownHook(atExitWorker);
    }

    private void onExit() {
        for (Runnable action : actions) {
            try {
                action.run();
            } catch (Throwable ex) {  // OK to catch Throwable here
                logger.error("Exception during shutdown hook", ex);
                // And then continue other hooks though
            }
        }
    }

}

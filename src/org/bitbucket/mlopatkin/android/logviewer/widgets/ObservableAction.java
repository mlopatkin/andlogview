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

package org.bitbucket.mlopatkin.android.logviewer.widgets;


import org.bitbucket.mlopatkin.utils.events.Observable;
import org.bitbucket.mlopatkin.utils.events.Subject;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * {@link Observable} Swing Action. Something can subscribe to Action events through Observable without the dependency
 * on Swing.
 */
public class ObservableAction extends AbstractAction {
    private final Subject<Runnable> observers = new Subject<>();

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Runnable observer : observers) {
            observer.run();
        }
    }

    public Observable<Runnable> asObservable() {
        return observers.asObservable();
    }
}

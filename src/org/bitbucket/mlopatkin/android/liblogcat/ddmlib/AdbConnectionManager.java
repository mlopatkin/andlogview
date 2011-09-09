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
package org.bitbucket.mlopatkin.android.liblogcat.ddmlib;

import org.bitbucket.mlopatkin.android.logviewer.Configuration;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.Log;

class AdbConnectionManager {
    private AdbConnectionManager() {
    }

    private static boolean inited = false;

    static AndroidDebugBridge getAdb() {
        if (!inited) {
            AndroidDebugBridge.init(false);
            AndroidDebugBridge.createBridge(Configuration.adb.executable(), false);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    closeAdb();
                }

            });
            inited = true;
        }
        return AndroidDebugBridge.getBridge();
    }

    static void closeAdb() {
        AndroidDebugBridge.terminate();
    }

    static {
        Log.setLogOutput(new DdmlibToLog4jWrapper());
    }
}

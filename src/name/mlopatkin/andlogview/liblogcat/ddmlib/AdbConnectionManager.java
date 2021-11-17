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
package name.mlopatkin.andlogview.liblogcat.ddmlib;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.Log;

import org.apache.log4j.Logger;

import java.lang.reflect.Field;

public class AdbConnectionManager {
    private static final Logger logger = Logger.getLogger(AdbConnectionManager.class);

    private AdbConnectionManager() {}

    private static boolean inited = false;
    private static boolean ready = false;

    private static boolean isReady(AndroidDebugBridge adb) throws DdmlibUnsupportedException {
        if (adb == null) {
            return false;
        }
        // hack below - there is no explicit way to check if the bridge was
        // created succesfully
        try {
            Field started = AndroidDebugBridge.class.getDeclaredField("mStarted");
            started.setAccessible(true);
            return started.getBoolean(adb);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.fatal("The DDMLIB is unsupported", e);
            throw new DdmlibUnsupportedException("The DDMLIB supplied is unsupported: " + System.getenv("DDMLIB"));
        }
    }

    public static void init(String adbExecutablePath) throws AdbException, DdmlibUnsupportedException {
        if (!inited) {
            Log.addLogger(new DdmlibToLog4jWrapper());
            if (System.getProperty("logview.debug.ddmlib") != null) {
                DdmPreferences.setLogLevel("debug");
            }
            logger.debug("ADB library initialization");
            AndroidDebugBridge.init(false);
            Runtime.getRuntime().addShutdownHook(new Thread(AdbConnectionManager::closeAdb));
            inited = true;
            try {
                AndroidDebugBridge adb = AndroidDebugBridge.createBridge(adbExecutablePath, false);
                if (isReady(adb)) {
                    ready = true;
                }
            } catch (IllegalArgumentException e) {
                logger.error(e);
                throw new AdbException("Cannot initialize ADB server. See logs for details");
            }
        }
    }

    public static boolean isFailed() {
        return inited && !ready;
    }

    public static boolean isReady() {
        return inited && ready;
    }

    private static void checkState() throws IllegalStateException {
        if (!inited || AndroidDebugBridge.getBridge() == null) {
            throw new IllegalStateException(
                    "Invalid DDMLIB state: inited=" + inited + " bridge=" + AndroidDebugBridge.getBridge());
        }
    }

    static AndroidDebugBridge getAdb() {
        checkState();
        return AndroidDebugBridge.getBridge();
    }

    static void closeAdb() {
        AndroidDebugBridge.terminate();
    }
}

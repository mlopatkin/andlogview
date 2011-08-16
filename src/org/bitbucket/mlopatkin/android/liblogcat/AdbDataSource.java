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
package org.bitbucket.mlopatkin.android.liblogcat;

import org.apache.log4j.Logger;

import com.android.ddmlib.AndroidDebugBridge;

public class AdbDataSource implements DataSource {

    private static final Logger logger = Logger.getLogger(AdbDataSource.class);

    private LogRecordDataSourceListener listener;

    private AndroidDebugBridge adb;

    public AdbDataSource() {
        AndroidDebugBridge.init(false);
        adb = AndroidDebugBridge.createBridge();
        if (adb == null) {
            logger.error("ADB is null");
        }
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public PidToProcessConverter getPidToProcessConverter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLogRecordListener(LogRecordDataSourceListener listener) {
        this.listener = listener;
    }

}

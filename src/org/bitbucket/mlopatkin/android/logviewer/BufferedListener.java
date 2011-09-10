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
package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Timer;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordDataSourceListener;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbDataSource;

/**
 * This class tries to limit the number of events pushing records into the
 * model.
 * <p>
 * If the source produces records with high frequency (e. g.
 * {@link AdbDataSource} at the startup) and for each record we create an event
 * then the event queue becomes overloaded with these events. The UI becomes
 * unresponsive. However, new records should appear as fast as possible.
 */
public class BufferedListener implements LogRecordDataSourceListener {

    private LogRecordTableModel model;
    private AutoScrollController scrollController;

    public BufferedListener(LogRecordTableModel model, AutoScrollController scrollController) {
        this.model = model;
        this.scrollController = scrollController;
        mergeTimer.setDelay(500);
        mergeTimer.start();
    }

    private List<LogRecord> internalBuffer = new ArrayList<LogRecord>();;
    private final Object lock = new Object();

    @Override
    public void onNewRecord(final LogRecord record) {
        synchronized (lock) {
            internalBuffer.add(record);
        }

    }

    private Timer mergeTimer = new Timer(0, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            mergeIntoModel();
        }
    });

    private void mergeIntoModel() {
        assert EventQueue.isDispatchThread();
        List<LogRecord> records = internalBuffer;
        synchronized (lock) {
            internalBuffer = new ArrayList<LogRecord>();
        }
        Collections.sort(records);
        scrollController.notifyBeforeInsert();
        model.append(records);
    }

    @Override
    public void assign(List<LogRecord> records) {
        final List<LogRecord> copy = new ArrayList<LogRecord>(records);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                model.assign(copy);
            }
        });
    }

}

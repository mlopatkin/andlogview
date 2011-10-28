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
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Timer;

import org.apache.log4j.Logger;
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
    private static final Logger logger = Logger.getLogger(BufferedListener.class);

    private LogRecordTableModel model;
    private AutoScrollController scrollController;

    public BufferedListener(LogRecordTableModel model, AutoScrollController scrollController) {
        this.model = model;
        this.scrollController = scrollController;
        mergeTimer.start();
        watchdogTimer.start();
    }

    private enum Policy {
        /**
         * Send a record to the event queue as soon as it arrives.
         */
        IMMEDIATE,
        /**
         * Store a record in the buffer and send a batch of them in the merger.
         */
        BUFFER;
    }

    private Policy policy = Policy.IMMEDIATE;

    private static final int MERGE_INTERVAL_MS = 500;
    private static final int WATCHDOG_INTERVAL_MS = 500;

    // maximal number of records that can be set to eventqueue between watchdog
    // invocations
    private static final int MAX_RECORDS_SPEED_THRESHOLD = 50;

    // minimal number of records that arrived between merger invokations to keep
    // buffering
    private static final int MIN_RECORDS_SPEED_THRESHOLD = 50;

    // the number of records sent into eventqueue between watchdog invocations
    private volatile AtomicInteger immediateCount = new AtomicInteger(0);

    private List<LogRecord> internalBuffer = new ArrayList<LogRecord>();;
    private final Object lock = new Object();

    @Override
    public void addRecord(final LogRecord record) {
        assert record != null;
        switch (policy) {
        case IMMEDIATE:
            sendRecordImmediate(record);
            break;
        case BUFFER:
            addRecordToBuffer(record);
            break;
        default:
            throw new IllegalStateException("Invalid policy value: " + policy);
        }
    }

    private void addRecordToBuffer(LogRecord record) {
        synchronized (lock) {
            internalBuffer.add(record);
        }
    }

    private void sendRecordImmediate(final LogRecord record) {
        int count = immediateCount.addAndGet(1);
        if (count >= MAX_RECORDS_SPEED_THRESHOLD) {
            setPolicy(Policy.BUFFER);
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                addOneRecord(record);
            }
        });
    }

    private Timer mergeTimer = new Timer(MERGE_INTERVAL_MS, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (policy == Policy.BUFFER) {
                mergeIntoModel();
            }
        }
    });

    private Timer watchdogTimer = new Timer(WATCHDOG_INTERVAL_MS, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            immediateCount.set(0);
        }
    });

    private void addOneRecord(LogRecord record) {
        assert EventQueue.isDispatchThread();
        scrollController.notifyBeforeInsert();
        model.addRecord(record);
    }

    private void mergeIntoModel() {
        assert EventQueue.isDispatchThread();
        List<LogRecord> records = internalBuffer;
        synchronized (lock) {
            internalBuffer = new ArrayList<LogRecord>();
        }
        if (records.size() < MIN_RECORDS_SPEED_THRESHOLD) {
            setPolicy(Policy.IMMEDIATE);
        }
        Collections.sort(records);
        scrollController.notifyBeforeInsert();
        model.append(records);
    }

    @Override
    public void setRecords(List<LogRecord> records) {
        final List<LogRecord> copy = new ArrayList<LogRecord>(records);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                model.assign(copy);
            }
        });
    }

    private void setPolicy(Policy policy) {
        this.policy = policy;
        logger.debug("Switched to " + policy);
    }
}

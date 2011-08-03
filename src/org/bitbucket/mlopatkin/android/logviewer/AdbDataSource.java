package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordStream;

public class AdbDataSource {
    private static final String ADB_BASE_COMMANDLINE = Configuration.adb.commandline();

    private LogRecordStream input;
    private LogRecordsTableModel model;

    public AdbDataSource(LogRecordsTableModel model) {
        this.model = model;
        ProcessBuilder pb = new ProcessBuilder(makeCommandLine());

        try {
            Process proc = pb.start();
            input = new LogRecordStream(proc.getInputStream());
            (new AdbPollingThread()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> makeCommandLine() {
        List<String> commands = new ArrayList<String>(splitCommandLine(ADB_BASE_COMMANDLINE));
        for (String buffer : Configuration.adb.buffers()) {
            commands.add(Configuration.adb.bufferswitch());
            commands.add(buffer);
        }
        return commands;
    }

    private static List<String> splitCommandLine(String commandLine) {
        StrTokenizer tokenizer = new StrTokenizer(commandLine, StrMatcher.splitMatcher(),
                StrMatcher.quoteMatcher());
        return tokenizer.getTokenList();
    }

    private void pushRecord(final LogRecord record) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                model.addRecord(record);
            }
        });
    }

    private class AdbPollingThread extends Thread {
        AdbPollingThread() {
            super("ADB-Polling");
            setDaemon(true);
        }

        @Override
        public void run() {
            LogRecord record = input.next();
            while (record != null) {
                pushRecord(record);
                record = input.next();
            }
        }
    }
}

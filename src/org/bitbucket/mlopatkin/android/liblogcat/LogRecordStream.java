package org.bitbucket.mlopatkin.android.liblogcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LogRecordStream {

    private BufferedReader in;

    public LogRecordStream(InputStream in) {
        this.in = new BufferedReader(new InputStreamReader(in));
    }

    public LogRecord next() {
        try {
            String record = in.readLine();
            while (record != null && (record.startsWith("-----") || record.isEmpty())) {
                record = in.readLine();
            }
            if (record != null) {
                return LogRecordParser.parseThreadtimeRecord(record);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordParser;

public class TestDataLoader {
    private static final String filename = "test_data.txt";

    public static List<LogRecord> getRecords() {
        List<LogRecord> result = new ArrayList<LogRecord>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            try {
                String s = in.readLine();
                while (s != null) {
                    result.add(LogRecordParser.parseThreadtimeRecord(s));
                    s = in.readLine();
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}

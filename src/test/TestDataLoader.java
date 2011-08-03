package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bitbucket.mlopatkin.android.logviewer.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.LogRecordParser;

public class TestDataLoader {
    private static final String filename = "test_data.txt";
    
    public static List<LogRecord> getRecords() {
        List<LogRecord> result = new ArrayList<LogRecord>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String s = in.readLine();
            while (s != null) {
                result.add(LogRecordParser.parseThreadtimeRecord(s));
                s = in.readLine();
            }            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}

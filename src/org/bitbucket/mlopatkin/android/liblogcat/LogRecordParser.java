package org.bitbucket.mlopatkin.android.liblogcat;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Pattern;


public class LogRecordParser {
    private LogRecordParser() {
    }

    static DateFormat dateTimeFormat = new SimpleDateFormat(
            "MM-dd HH:mm:ss.SSS");

    private static final Pattern tagSeparator = Pattern.compile(": ");
    
    public static LogRecord parseThreadtimeRecord(String s) {
        ParsePosition pos = new ParsePosition(0);
        Date dateTime = dateTimeFormat.parse(s, pos);
        Scanner scanner = new Scanner(s.substring(pos.getIndex()));
        int pid = scanner.nextInt();
        int tid = scanner.nextInt();
        LogRecord.Priority priority = getPriorityFromChar(scanner.next());
        String tag = readTag(scanner);
        scanner.skip(tagSeparator);
        String message = scanner.nextLine();
        return new LogRecord(dateTime, pid, tid, priority, tag, message);
    }

    private static String readTag(Scanner scanner) {
        return scanner.useDelimiter(tagSeparator).next().trim();
    }

    private static LogRecord.Priority getPriorityFromChar(String next) {
        next = next.trim();
        for (LogRecord.Priority val : LogRecord.Priority.values()) {
            if (val.getLetter().equalsIgnoreCase(next)) {
                return val;
            }
        }
        throw new IllegalArgumentException("Symbol '" + next
                + "' doesn't correspond to valid priority value");
    }
}

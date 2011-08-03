package org.bitbucket.mlopatkin.android.liblogcat;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeFormatUtils {

    private TimeFormatUtils() {

    }

    private static final DateFormat LOGCAT_DATE_FORMAT = new SimpleDateFormat(
            "MM-dd HH:mm:ss.SSS");
    
    public static Date getTimeFromString(String s, ParsePosition pos) {
        return LOGCAT_DATE_FORMAT.parse(s, pos);
    }
    
    public static String convertTimeToString(Date date) {
        return LOGCAT_DATE_FORMAT.format(date);
    }
}

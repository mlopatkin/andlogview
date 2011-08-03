package org.bitbucket.mlopatkin.android.liblogcat;

import java.util.Date;

public class LogRecord {
    public enum Priority {
        ASSERT, DEBUG, ERROR, VERBOSE, WARN, INFO;

        String getLetter() {
            return toString().substring(0, 1);
        }

    }

    private Date time;
    private int pid;
    private int tid;
    private Priority priority;
    private String tag;
    private String message;

    public LogRecord(Date time, int pid, int tid, Priority priority, String tag, String message) {
        this.time = time;
        this.pid = pid;
        this.tid = tid;
        this.priority = priority;
        this.tag = tag;
        this.message = message;
    }

    public Date getTime() {
        return time;
    }

    public int getPid() {
        return pid;
    }

    public int getTid() {
        return tid;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getTag() {
        return tag;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(TimeFormatUtils.convertTimeToString(time)).append('\t');
        b.append(pid).append('\t');
        b.append(tid).append('\t');
        b.append(priority.getLetter()).append('\t');
        b.append(tag).append('\t');
        b.append(message);
        return b.toString();
    }

}

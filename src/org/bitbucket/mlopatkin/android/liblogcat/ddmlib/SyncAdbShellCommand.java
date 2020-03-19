package org.bitbucket.mlopatkin.android.liblogcat.ddmlib;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.MultiLineReceiver;

class SyncAdbShellCommand extends AdbShellCommand<IShellOutputReceiver> {
    private static class StringReceiver extends MultiLineReceiver {
        final StringBuilder linesBuf = new StringBuilder();

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                linesBuf.append(line);
            }
        }
    }

    private SyncAdbShellCommand(IDevice device, String commandLine, StringReceiver receiver) {
        super(device, commandLine, receiver);
    }

    @Override
    protected void onCommandFinished(IShellOutputReceiver outputReceiver) {
        super.onCommandFinished(outputReceiver);
    }

    @Override
    protected void onException(Exception e, IShellOutputReceiver outputReceiver) {
        super.onException(e, outputReceiver);
    }

    public static String execute(IDevice device, String commandLine) {
        StringReceiver receiver = new StringReceiver();
        SyncAdbShellCommand cmd = new SyncAdbShellCommand(device, commandLine, receiver);
        // Run is used intentionally
        cmd.run();
        return receiver.linesBuf.toString();
    }
}

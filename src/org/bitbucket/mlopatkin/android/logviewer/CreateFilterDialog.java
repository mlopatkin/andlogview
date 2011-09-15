package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.Frame;

public class CreateFilterDialog extends FilterDialog {

    private DialogResultReceiver receiver;

    protected CreateFilterDialog(Frame owner, DialogResultReceiver resultReceiver) {
        super(owner);
        setTitle("Create new filter");
        receiver = resultReceiver;
    }

    interface DialogResultReceiver {
        void onDialogResult(CreateFilterDialog result, boolean success);
    }

    protected void onPositiveResult() {
        assert receiver != null;
        if (!isInputValid()) {
            return;
        }
        receiver.onDialogResult(this, true);
        receiver = null;
        setVisible(false);
    }

    protected void onNegativeResult() {
        assert receiver != null;
        receiver.onDialogResult(this, false);
        receiver = null;
        setVisible(false);
    }

    public static void startCreateFilterDialog(Frame owner, DialogResultReceiver resultReceiver) {
        if (resultReceiver == null) {
            throw new NullPointerException("resultReceiver can't be null");
        }
        CreateFilterDialog dialog = new CreateFilterDialog(owner, resultReceiver);
        dialog.setVisible(true);
    }
}

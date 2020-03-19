package org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog;

import com.google.common.base.Optional;

import org.bitbucket.mlopatkin.android.logviewer.ErrorDialogsHelper;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;

import java.awt.Frame;

public class CreateFilterDialog extends FilterDialog {
    private DialogResultReceiver receiver;

    protected CreateFilterDialog(Frame owner, DialogResultReceiver resultReceiver) {
        super(owner);
        setTitle("Create new filter");
        receiver = resultReceiver;
    }

    public interface DialogResultReceiver {
        void onDialogResult(Optional<FilterFromDialog> filter);
    }

    @Override
    protected void onPositiveResult() {
        assert receiver != null;
        try {
            receiver.onDialogResult(Optional.of(createFilter()));
        } catch (RequestCompilationException e) {
            ErrorDialogsHelper.showError(
                    this, "%s is not a valid search expression: %s", e.getRequestValue(), e.getMessage());
            return;
        }
        receiver = null;
        setVisible(false);
    }

    @Override
    protected void onNegativeResult() {
        assert receiver != null;
        receiver.onDialogResult(Optional.<FilterFromDialog>absent());
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

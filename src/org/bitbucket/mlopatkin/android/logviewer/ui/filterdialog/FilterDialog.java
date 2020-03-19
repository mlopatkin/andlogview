package org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;
import org.bitbucket.mlopatkin.android.logviewer.config.Configuration;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilteringMode;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import static org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilteringModesPanel.ModeChangedListener;

/**
 * Common GUI logic related to filtering.
 */
@VisibleForTesting
public abstract class FilterDialog extends BaseFilterDialogUi {
    /**
     * Create the dialog.
     */
    protected FilterDialog(Frame owner) {
        super(owner);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onPositiveResult();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onNegativeResult();
            }
        });

        ModeChangedListener modeListener = new ModeChangedListener() {
            @Override
            public void modeSelected(FilteringMode mode) {
                colorsList.setVisible(mode == FilteringMode.HIGHLIGHT);
                colorsList.revalidate();
                colorsList.repaint();
            }
        };
        modesPanel.setModeChangedListener(modeListener);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                onNegativeResult();
            }
        });
    }

    protected abstract void onPositiveResult();

    protected abstract void onNegativeResult();

    private static final Splitter commaSplitter = Splitter.on(',').trimResults(CharMatcher.whitespace());

    private List<String> getTags() {
        String tagsString = Strings.nullToEmpty(tagTextField.getText());
        if (!CharMatcher.whitespace().matchesAllOf(tagsString)) {
            return commaSplitter.splitToList(tagsString);
        }
        return Collections.emptyList();
    }

    @Nullable
    private String getMessageText() {
        String message = messageTextField.getText();
        if (!CharMatcher.whitespace().matchesAllOf(message)) {
            return message;
        }
        return null;
    }

    private List<Integer> getPids() {
        String pidString = Strings.nullToEmpty(pidTextField.getText());
        if (!CharMatcher.whitespace().matchesAllOf(pidString)) {
            List<Integer> pids = new ArrayList<>();
            for (String pid : commaSplitter.split(pidString)) {
                try {
                    pids.add(Integer.parseInt(pid));
                } catch (NumberFormatException e) {
                    // ignore, let it go to the appName
                }
            }
            return pids;
        }
        return Collections.emptyList();
    }

    private List<String> getAppNames() {
        String pidString = pidTextField.getText();
        if (!CharMatcher.whitespace().matchesAllOf(pidString)) {
            List<String> appNames = new ArrayList<>();
            for (String item : commaSplitter.split(pidString)) {
                if (!CharMatcher.inRange('0', '9').matchesAllOf(item)) {
                    appNames.add(item);
                }
            }
            return appNames;
        } else {
            return Collections.emptyList();
        }
    }

    private LogRecord.Priority getPriority() {
        return (Priority) logLevelList.getSelectedItem();
    }

    private FilteringMode getFilteringMode() {
        return modesPanel.getSelectedMode();
    }

    protected JTextField getTagTextField() {
        return tagTextField;
    }

    protected JTextField getMessageTextField() {
        return messageTextField;
    }

    protected JTextField getPidTextField() {
        return pidTextField;
    }

    protected JComboBox getLogLevelList() {
        return logLevelList;
    }

    protected FilteringModesPanel getModePanel() {
        return modesPanel;
    }

    private Color getSelectedColor() {
        if (getFilteringMode() == FilteringMode.HIGHLIGHT) {
            return Configuration.ui.highlightColors().get(colorsList.getSelectedIndex());
        } else {
            return null;
        }
    }

    protected void setSelectedColor(Color color) {
        int index = 0;
        for (Color current : Configuration.ui.highlightColors()) {
            if (current.equals(color)) {
                colorsList.setSelectedIndex(index);
                return;
            } else {
                ++index;
            }
        }
    }

    protected FilterFromDialog createFilter() throws RequestCompilationException {
        FilterFromDialog filter = new FilterFromDialog();
        filter.setTags(getTags());
        filter.setPids(getPids());
        filter.setApps(getAppNames());
        filter.setMode(getFilteringMode());
        filter.setPriority(getPriority());
        filter.setHighlightColor(getSelectedColor());
        filter.setMessagePattern(getMessageText());
        filter.initialize();
        return filter;
    }
}

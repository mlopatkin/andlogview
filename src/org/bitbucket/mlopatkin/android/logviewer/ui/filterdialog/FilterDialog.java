package org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;
import org.bitbucket.mlopatkin.android.logviewer.ErrorDialogsHelper;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilteringMode;
import org.bitbucket.mlopatkin.android.logviewer.config.Configuration;
import org.bitbucket.mlopatkin.android.logviewer.search.RequestCompilationException;
import org.bitbucket.mlopatkin.android.logviewer.search.SearchStrategyFactory;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import static org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilteringModesPanel.ModeChangedListener;

/**
 * Common GUI logic related to filtering.
 */
abstract class FilterDialog extends BaseFilterDialogUi {

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

    private static final Splitter commaSplitter =
            Splitter.on(',').trimResults(CharMatcher.WHITESPACE);

    private String[] getTags() {
        String tagsString = Strings.nullToEmpty(tagTextField.getText());
        if (!CharMatcher.WHITESPACE.matchesAllOf(tagsString)) {
            return Iterables.toArray(commaSplitter.splitToList(tagsString), String.class);
        }
        return null;
    }

    private String getMessageText() {
        String message = messageTextField.getText();
        if (!CharMatcher.WHITESPACE.matchesAllOf(message)) {
            return message;
        }
        return null;
    }

    private List<Integer> getPids() {
        String pidString = Strings.nullToEmpty(pidTextField.getText());
        if (!CharMatcher.WHITESPACE.matchesAllOf(pidString)) {
            List<Integer> pids = new ArrayList<Integer>();
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
        if (!CharMatcher.WHITESPACE.matchesAllOf(pidString)) {
            List<String> appNames = new ArrayList<String>();
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


    protected boolean isInputValid() {
        List<String> appNames = getAppNames();
        for (String appName : appNames) {
            try {
                SearchStrategyFactory.createSearchStrategy(appName);
            } catch (RequestCompilationException e) {
                ErrorDialogsHelper.showError(this, "%s is not a valid search expression: %s",
                        appName, e.getMessage());
                return false;
            }
        }
        String request = getMessageText();
        try {
            SearchStrategyFactory.createSearchStrategy(request);
        } catch (RequestCompilationException e) {
            ErrorDialogsHelper.showError(this, "%s is not a valid search expression: %s",
                    request, e.getMessage());
            return false;
        }
        return true;
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

    protected JComboBox getColorsList() {
        return colorsList;
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

    public FilterFromDialog createFilter() {
        FilterFromDialog filter = new FilterFromDialog();
        filter.setPids(getPids());
        filter.setApps(getAppNames());
        filter.setMode(getFilteringMode());
        filter.setPriority(getPriority());
        filter.setHighlightColor(getSelectedColor());
        filter.setMessagePattern(getMessageText());
        try {
            filter.initialize();
        } catch (RequestCompilationException e) {
            throw new AssertionError("Must be validated");
        }
        return filter;
    }
}

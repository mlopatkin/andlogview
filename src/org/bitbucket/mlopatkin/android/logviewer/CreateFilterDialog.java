package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;
import org.bitbucket.mlopatkin.android.liblogcat.FilterToText;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordFilter;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;

public class CreateFilterDialog extends JDialog {

    private final JPanel contentPanel = new JPanel();
    private JTextField tagTextField;
    private JTextField messageTextField;
    private JTextField pidTextField;

    private DialogResultReceiver receiver;
    private JComboBox logLevelList;
    private JRadioButton showRadioBtn;
    private JRadioButton highlightRadioBtn;
    private JRadioButton hideRadioBtn;

    /**
     * Create the dialog.
     */
    public CreateFilterDialog() {
        initialize();
    }

    private void initialize() {
        setTitle("Create new filter");
        setBounds(100, 100, 529, 374);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        {
            tagTextField = new JTextField();
            tagTextField.setColumns(10);
        }

        JLabel lblNewLabel = new JLabel("Tags to filter");

        final ButtonGroup btngrpFilterAction = new ButtonGroup();

        showRadioBtn = new JRadioButton("Hide other");
        btngrpFilterAction.add(showRadioBtn);

        highlightRadioBtn = new JRadioButton("Highlight");
        btngrpFilterAction.add(highlightRadioBtn);
        highlightRadioBtn.setSelected(true);

        JLabel lblMessageTextTo = new JLabel("Message text to filter");

        messageTextField = new JTextField();
        messageTextField.setColumns(10);

        JLabel lblPidsToFilter = new JLabel("PIDs to filter");

        pidTextField = new JTextField();
        pidTextField.setColumns(10);

        JLabel lblLogLevel = new JLabel("Log level");

        logLevelList = new JComboBox(new PriorityComboBoxModel());

        hideRadioBtn = new JRadioButton("Hide this");
        btngrpFilterAction.add(hideRadioBtn);
        GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
        gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        gl_contentPanel.createSequentialGroup().addContainerGap().addGroup(
                                gl_contentPanel.createParallelGroup(Alignment.LEADING)
                                        .addComponent(highlightRadioBtn).addComponent(tagTextField,
                                                GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                                        .addComponent(lblNewLabel).addGroup(
                                                gl_contentPanel.createSequentialGroup()
                                                        .addComponent(showRadioBtn)
                                                        .addPreferredGap(
                                                                ComponentPlacement.UNRELATED)
                                                        .addComponent(hideRadioBtn)).addComponent(
                                                lblMessageTextTo).addComponent(messageTextField,
                                                GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                                        .addComponent(lblPidsToFilter).addComponent(pidTextField,
                                                GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                                        .addComponent(lblLogLevel).addComponent(logLevelList, 0,
                                                487, Short.MAX_VALUE)).addContainerGap()));
        gl_contentPanel.setVerticalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        gl_contentPanel.createSequentialGroup().addComponent(lblNewLabel)
                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(
                                        tagTextField, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(
                                        lblMessageTextTo).addPreferredGap(
                                        ComponentPlacement.RELATED).addComponent(messageTextField,
                                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE).addPreferredGap(
                                        ComponentPlacement.UNRELATED).addComponent(lblPidsToFilter)
                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(
                                        pidTextField, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.UNRELATED).addComponent(
                                        lblLogLevel).addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(logLevelList, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                                .addGroup(
                                        gl_contentPanel.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(showRadioBtn).addComponent(
                                                        hideRadioBtn)).addPreferredGap(
                                        ComponentPlacement.UNRELATED).addComponent(
                                        highlightRadioBtn).addGap(30)));
        contentPanel.setLayout(gl_contentPanel);
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onPositiveResult();
                    }
                });
            }
            {
                JButton cancelButton = new JButton("Cancel");
                buttonPane.add(cancelButton);
                cancelButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onNegativeResult();
                    }
                });
            }
        }
    }

    void startDialogForResult(DialogResultReceiver resultReceiver) {
        if (resultReceiver == null) {
            throw new NullPointerException("resultReceiver can't be null");
        }
        receiver = resultReceiver;
        resetDialog();
        setVisible(true);
    }

    void startEditDialogForResult(DialogResultReceiver resultReceiver, LogRecordFilter filter) {
        if (resultReceiver == null) {
            throw new NullPointerException("resultReceiver can't be null");
        }
        receiver = resultReceiver;
        tagTextField.setText(FilterToText.getTags(filter));
        pidTextField.setText(FilterToText.getPids(filter));
        messageTextField.setText(FilterToText.getMessage(filter));
        logLevelList.setSelectedItem(FilterToText.getPriority(filter));

        setVisible(true);
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
        receiver = null;
        setVisible(false);
    }

    public String[] getTags() {
        String tagsString = tagTextField.getText();
        if (StringUtils.isNotBlank(tagsString)) {
            String tags[] = StringUtils.split(tagsString, ',');
            for (int i = 0; i < tags.length; ++i) {
                tags[i] = tags[i].trim();
            }
            return tags;
        }
        return null;
    }

    public String getMessageText() {
        String message = messageTextField.getText();
        if (StringUtils.isNotBlank(message)) {
            return message;
        }
        return null;
    }

    public int[] getPids() {
        String pidString = pidTextField.getText();
        if (StringUtils.isNotBlank(pidString)) {
            String pidStrings[] = StringUtils.split(pidString, ',');
            int pids[] = new int[pidStrings.length];
            for (int i = 0; i < pids.length; ++i) {
                pids[i] = Integer.parseInt(pidStrings[i].trim());
            }
            return pids;
        }
        return null;
    }

    public LogRecord.Priority getPriority() {
        return (Priority) logLevelList.getSelectedItem();
    }

    public boolean isHighlightMode() {
        return highlightRadioBtn.isSelected();
    }

    public boolean isShowMode() {
        return showRadioBtn.isSelected();
    }

    public boolean isHideMode() {
        return hideRadioBtn.isSelected();
    }

    private class PriorityComboBoxModel extends AbstractListModel implements ComboBoxModel {

        private Object selected;

        @Override
        public Object getSelectedItem() {
            return selected;
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selected = anItem;
        }

        @Override
        public Object getElementAt(int index) {
            if (index == 0) {
                return null;
            }
            return LogRecord.Priority.values()[index - 1];
        }

        @Override
        public int getSize() {
            return LogRecord.Priority.values().length + 1;
        }

    }

    private boolean isInputValid() {
        try {
            getPids();
            return true;
        } catch (NumberFormatException e) {
            return false;
        }

    }

    private void resetDialog() {
        tagTextField.setText(null);
        messageTextField.setText(null);
        pidTextField.setText(null);
        logLevelList.setSelectedIndex(0);
        highlightRadioBtn.setSelected(true);
        tagTextField.requestFocusInWindow();
    }

    protected JTextField getTagText() {
        return tagTextField;
    }

    protected JTextField getTfMessage() {
        return messageTextField;
    }

    protected JTextField getPidTextField() {
        return pidTextField;
    }

    protected JComboBox getLogLevelList() {
        return logLevelList;
    }
}

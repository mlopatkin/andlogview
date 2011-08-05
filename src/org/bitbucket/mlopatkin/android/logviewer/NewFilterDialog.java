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

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;

public class NewFilterDialog extends JDialog {

    private final JPanel contentPanel = new JPanel();
    private JTextField tagText;
    private JTextField messageText;
    private JTextField pidText;

    private DialogResultReceiver receiver;
    private JComboBox cbLogLevel;
    private JRadioButton rdbtnNewRadioButton;
    private JRadioButton rdbtnHighlight;

    /**
     * Create the dialog.
     */
    public NewFilterDialog() {
        initialize();
    }

    private void initialize() {
        setTitle("Create new filter");
        setBounds(100, 100, 529, 374);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        {
            tagText = new JTextField();
            tagText.setColumns(10);
        }

        JLabel lblNewLabel = new JLabel("Tags to filter");

        final ButtonGroup btngrpFilterAction = new ButtonGroup();

        rdbtnNewRadioButton = new JRadioButton("Hide other");
        btngrpFilterAction.add(rdbtnNewRadioButton);

        rdbtnHighlight = new JRadioButton("Highlight");
        btngrpFilterAction.add(rdbtnHighlight);
        rdbtnHighlight.setSelected(true);

        JLabel lblMessageTextTo = new JLabel("Message text to filter");

        messageText = new JTextField();
        messageText.setColumns(10);

        JLabel lblPidsToFilter = new JLabel("PIDs to filter");

        pidText = new JTextField();
        pidText.setColumns(10);

        JLabel lblLogLevel = new JLabel("Log level");

        cbLogLevel = new JComboBox(new PriorityComboBoxModel());
        GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
        gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        gl_contentPanel.createSequentialGroup().addContainerGap().addGroup(
                                gl_contentPanel.createParallelGroup(Alignment.LEADING)
                                        .addComponent(rdbtnHighlight).addComponent(tagText,
                                                GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
                                        .addComponent(lblNewLabel)
                                        .addComponent(rdbtnNewRadioButton).addComponent(
                                                lblMessageTextTo).addComponent(messageText,
                                                GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                                        .addComponent(lblPidsToFilter).addComponent(pidText,
                                                GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                                        .addComponent(lblLogLevel).addComponent(cbLogLevel, 0, 492,
                                                Short.MAX_VALUE)).addContainerGap()));
        gl_contentPanel.setVerticalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        gl_contentPanel.createSequentialGroup().addComponent(lblNewLabel)
                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(tagText,
                                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE).addPreferredGap(
                                        ComponentPlacement.RELATED).addComponent(lblMessageTextTo)
                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(
                                        messageText, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.UNRELATED).addComponent(
                                        lblPidsToFilter)
                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(
pidText,
                                        GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.UNRELATED).addComponent(
                                        lblLogLevel).addPreferredGap(ComponentPlacement.RELATED)
.addComponent(
                                        cbLogLevel, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                                .addComponent(rdbtnNewRadioButton).addPreferredGap(
                                        ComponentPlacement.UNRELATED).addComponent(rdbtnHighlight)
                                .addGap(30)));
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
        if (receiver == null) {
            throw new NullPointerException("resultReceiver can't be null");
        }
        receiver = resultReceiver;
        resetDialog();
        setVisible(true);
    }
    
    interface DialogResultReceiver {
        void onDialogResult(boolean success);
    }

    private void onPositiveResult() {
        assert receiver != null;
        if (isInputValid())
        if (receiver != null) {
            receiver.onDialogResult(true);
        }
        receiver = null;
        setVisible(false);
    }

    private void onNegativeResult() {
        assert receiver != null;
        if (receiver != null) {
            receiver.onDialogResult(false);
        }
        receiver = null;
        setVisible(false);
    }

    public String[] getTags() {
        return null;
    }

    public String getMessageText() {
        return null;
    }

    public int[] getPids() {
        return null;
    }

    public LogRecord.Priority getPriority() {
        return (Priority) cbLogLevel.getSelectedItem();
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
        return true;
    }

    private void resetDialog() {
        tagText.setText(null);
        tagText.requestFocusInWindow();
    }
}

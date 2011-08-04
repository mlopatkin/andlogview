package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

public class NewFilterDialog extends JDialog {

    private final JPanel contentPanel = new JPanel();
    private JTextField tagText;

    /**
     * Create the dialog.
     */
    public NewFilterDialog() {
        setTitle("Create new filter");
        setBounds(100, 100, 453, 169);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        {
            tagText = new JTextField();
            tagText.setColumns(10);
        }

        JLabel lblNewLabel = new JLabel("Tags to filter");
        GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
        gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        gl_contentPanel.createSequentialGroup().addContainerGap().addGroup(
                                gl_contentPanel.createParallelGroup(Alignment.LEADING)
                                        .addComponent(tagText, GroupLayout.DEFAULT_SIZE, 412,
                                                Short.MAX_VALUE).addComponent(lblNewLabel))
                                .addContainerGap()));
        gl_contentPanel.setVerticalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        gl_contentPanel.createSequentialGroup().addComponent(lblNewLabel)
                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(tagText,
                                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE).addContainerGap(183,
                                        Short.MAX_VALUE)));
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

    private DialogResultReceiver receiver;

    void startDialogForResult(DialogResultReceiver resultReceiver) {
        receiver = resultReceiver;
        tagText.setText(null);
        setVisible(true);
    }
    
    interface DialogResultReceiver {
        void onDialogResult(String tag);
    }

    private void onPositiveResult() {
        if (receiver != null) {
            receiver.onDialogResult(tagText.getText());
        }
        receiver = null;
        setVisible(false);
    }

    private void onNegativeResult() {
        if (receiver != null) {
            receiver.onDialogResult(null);
        }
        receiver = null;
        setVisible(false);
    }
}

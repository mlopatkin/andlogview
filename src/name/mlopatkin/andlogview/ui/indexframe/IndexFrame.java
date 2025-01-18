/*
 * Copyright 2011 Mikhail Lopatkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package name.mlopatkin.andlogview.ui.indexframe;

import static name.mlopatkin.andlogview.ui.indexframe.IndexFrameDi.FOR_INDEX_FRAME;

import name.mlopatkin.andlogview.LogRecordsTransferHandler;
import name.mlopatkin.andlogview.ui.AppFrame;
import name.mlopatkin.andlogview.ui.logtable.LogRecordTableColumnModel;
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;
import name.mlopatkin.andlogview.widgets.DecoratingRendererTable;
import name.mlopatkin.andlogview.widgets.UiHelper;
import name.mlopatkin.andlogview.widgets.UiHelper.DoubleClickListener;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

@IndexFrameScoped
public class IndexFrame extends AppFrame {
    private final DecoratingRendererTable indexedRecordsTable;
    private final IndexController controller;
    private final Component owner;
    private boolean isFirstShow = true;

    @Inject
    public IndexFrame(DialogFactory dialogFactory, LogRecordTableColumnModel columnsModel,
            @Named(FOR_INDEX_FRAME) JTable logTable, IndexController controller) {
        super(controller.getTitle());
        // TODO rethink this dependency
        this.owner = dialogFactory.getOwner();
        this.controller = controller;
        // TODO(mlopatkin) Replace this cast with injection
        this.indexedRecordsTable = (DecoratingRendererTable) logTable;
        initialize();
        indexedRecordsTable.setColumnModel(columnsModel);
        indexedRecordsTable.setTransferHandler(new LogRecordsTransferHandler());

        WindowListener closingListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.onWindowClosed();
            }
        };
        addWindowListener(closingListener);
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);

        indexedRecordsTable.setFillsViewportHeight(true);
        indexedRecordsTable.setShowGrid(false);
        UiHelper.addDoubleClickListener(indexedRecordsTable, new LineDoubleClickListener());
        scrollPane.setViewportView(indexedRecordsTable);

        setupKeys();
    }

    private class LineDoubleClickListener implements DoubleClickListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            int rowView = indexedRecordsTable.rowAtPoint(e.getPoint());
            if (rowView >= 0) {
                int row = indexedRecordsTable.convertRowIndexToModel(rowView);
                controller.activateRow(row);
            }
        }
    }

    private static final String KEY_JUMP_TO_LINE = "ENTER";
    private static final String ACTION_JUMP_TO_LINE = "jump_to_line";

    private void setupKeys() {
        UiHelper.bindKeyFocused(indexedRecordsTable, KEY_JUMP_TO_LINE, ACTION_JUMP_TO_LINE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = indexedRecordsTable.getSelectedRow();
                if (row >= 0) {
                    row = indexedRecordsTable.convertRowIndexToModel(row);
                    controller.activateRow(row);
                }
            }
        });
    }

    @Override
    public void setVisible(boolean b) {
        if (b && isFirstShow) {
            assert owner.isVisible();
            setPreferredSize(new Dimension(owner.getWidth(), getPreferredSize().height));
            pack();
            setLocationRelativeTo(owner);
            isFirstShow = false;
        }
        super.setVisible(b);
    }
}

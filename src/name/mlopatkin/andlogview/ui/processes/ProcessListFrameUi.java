/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.processes;

import java.awt.BorderLayout;

import javax.inject.Inject;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

public class ProcessListFrameUi {
    private final JFrame processListFrame;
    private final JTable processListTable;

    @Inject
    public ProcessListFrameUi() {
        processListFrame = new JFrame();

        processListFrame.setTitle("Processes");
        processListFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        processListFrame.setContentPane(contentPane);

        processListTable = new JTable();
        processListTable.setFillsViewportHeight(true);
        processListTable.setAutoCreateRowSorter(true);
        processListTable.setAutoCreateColumnsFromModel(false);
        contentPane.add(new JScrollPane(processListTable), BorderLayout.CENTER);

        processListFrame.pack();
    }

    public JFrame getFrame() {
        return processListFrame;
    }

    public JTable getProcessTable() {
        return processListTable;
    }
}

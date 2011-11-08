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
package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.KernelLogRecord;

public class KernelLogFrame extends JFrame {

    private final KernelLogListModel kernelLogModel = new KernelLogListModel();
    private final JList kernelLogList = new JList(kernelLogModel);
    private final ListScrollController scrollController = new ListScrollController(kernelLogList);
    private final BufferedListener<KernelLogRecord> kernelLogListener = new BufferedListener<KernelLogRecord>(
            kernelLogModel, scrollController);

    public KernelLogFrame() {
        setTitle("Kernel log");
        setBounds(100, 100, 450, 300);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JScrollPane scrollPane = new JScrollPane();
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        scrollPane.setViewportView(kernelLogList);
    }

    public boolean setSource(DataSource source) {
        if (source == null) {
            return false;
        }
        kernelLogModel.clear();
        return source.setKernelLogListener(kernelLogListener);
    }
}

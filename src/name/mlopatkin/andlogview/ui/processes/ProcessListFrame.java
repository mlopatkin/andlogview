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
package name.mlopatkin.andlogview.ui.processes;

import name.mlopatkin.andlogview.config.Configuration;
import name.mlopatkin.andlogview.logmodel.DataSource;

import com.google.common.collect.ImmutableList;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.RowFilter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Displays list of available processes and their pids.
 */
public class ProcessListFrame {
    private static final int UPDATE_DELAY_MS = 1000;
    private static final TableModel EMPTY_MODEL = new DefaultTableModel();
    private static final ImmutableList<SortKey> DEFAULT_SORTING =
            ImmutableList.of(new SortKey(ProcessListModel.COLUMN_PID, SortOrder.ASCENDING));

    private final JFrame owner;
    private final ProcessListFrameUi frameUi = new ProcessListFrameUi();

    private @Nullable ProcessListModel model;

    private final Timer updateTimer = new Timer(UPDATE_DELAY_MS, e -> {
        if (model != null) {
            model.update();
        }
    });

    public ProcessListFrame(JFrame owner) {
        this.owner = owner;
    }

    private void reset() {
        updateTimer.stop();
        model = null;
        frameUi.getProcessTable().setModel(EMPTY_MODEL);
    }

    public void setSource(@Nullable DataSource source) {
        assert SwingUtilities.isEventDispatchThread();
        if (source != null) {
            assert source.getPidToProcessConverter() != null;
            setModel(new ProcessListModel(source.getPidToProcessConverter()));
        } else {
            reset();
            if (getFrame().isVisible()) {
                hide();
            }
        }
    }

    private void setModel(ProcessListModel model) {
        var table = frameUi.getProcessTable();
        this.model = model;

        var oldKeys = table.getRowSorter().getSortKeys();
        if (oldKeys.isEmpty()) {
            oldKeys = DEFAULT_SORTING;
        }
        table.setModel(model);
        table.setColumnModel(new ColumnModel());
        var rowSorter = new TableRowSorter<>(model);
        rowSorter.setRowFilter(new ProcessesRowFilter());
        rowSorter.setSortKeys(oldKeys);
        table.setRowSorter(rowSorter);

        if (!updateTimer.isRunning()) {
            updateTimer.start();
        }
    }

    public void show() {
        var frame = getFrame();
        Point position = Configuration.ui.processWindowPosition();
        if (position == null) {
            frame.setLocationRelativeTo(owner);
        } else {
            frame.setLocation(position);
        }
        frame.setVisible(true);
    }

    public void hide() {
        var frame = getFrame();
        Configuration.ui.processWindowPosition(frame.getLocation());
        frame.setVisible(false);
    }

    private JFrame getFrame() {
        return frameUi.getFrame();
    }

    private static class ProcessesRowFilter extends RowFilter<ProcessListModel, Integer> {
        private static final Set<String> HIDDEN_PROCESSES = new HashSet<>();

        static {
            HIDDEN_PROCESSES.add("ps");
            HIDDEN_PROCESSES.add("logcat");
            HIDDEN_PROCESSES.add("/system/bin/sh");
            HIDDEN_PROCESSES.add("No info available");
        }

        @Override
        public boolean include(javax.swing.RowFilter.Entry<? extends ProcessListModel, ? extends Integer> entry) {
            ProcessListModel model = entry.getModel();
            int row = entry.getIdentifier();
            String processName = (String) model.getValueAt(row, ProcessListModel.COLUMN_PROCESS);
            return !HIDDEN_PROCESSES.contains(processName);
        }
    }
}

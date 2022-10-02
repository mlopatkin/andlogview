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
package name.mlopatkin.andlogview;

import name.mlopatkin.andlogview.liblogcat.file.FileDataSourceFactory;
import name.mlopatkin.andlogview.liblogcat.file.UnrecognizedFormatException;
import name.mlopatkin.andlogview.logmodel.DataSource;

import org.apache.log4j.Logger;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;

import javax.swing.TransferHandler;

public class FileTransferHandler extends TransferHandler {
    private static final Logger logger = Logger.getLogger(FileTransferHandler.class);
    private MainFrame frame;

    public FileTransferHandler(MainFrame frame) {
        this.frame = frame;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        Transferable t = support.getTransferable();

        try {
            @SuppressWarnings("unchecked")
            java.util.List<File> l = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);

            File file = l.get(0);
            logger.debug("Start importing " + file);
            DataSource source;
            try {
                source = FileDataSourceFactory.createDataSource(file);
                frame.setSource(source);
            } catch (UnrecognizedFormatException e) {
                ErrorDialogsHelper.showError(frame, "Unrecognized file format for " + file);
                return false;
            } catch (IOException e) {
                ErrorDialogsHelper.showError(frame, "Cannot read " + file);
                return false;
            }
            frame.setRecentDir(file.getAbsoluteFile().getParentFile());
        } catch (UnsupportedFlavorException | IOException e) {
            return false;
        }
        return true;
    }
}

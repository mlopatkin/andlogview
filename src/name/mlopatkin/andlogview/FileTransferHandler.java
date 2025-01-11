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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.TransferHandler;

public class FileTransferHandler extends TransferHandler {
    private static final Logger logger = LoggerFactory.getLogger(FileTransferHandler.class);
    private final MainFrame frame;

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
            List<File> l = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
            File file = l.get(0);
            logger.debug("Start importing {}", file);

            frame.openFile(file);
        } catch (UnsupportedFlavorException | IOException e) {
            return false;
        }
        return true;
    }
}

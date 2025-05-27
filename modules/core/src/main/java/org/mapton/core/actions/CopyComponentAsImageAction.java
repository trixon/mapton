/*
 * Copyright 2025 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mapton.core.actions;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(
        category = "Edit",
        id = "org.mapton.core.actions.CopyComponentAsImageAction"
)
@ActionRegistration(
        displayName = "#CTL_CopyComponentAsImageAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/Edit", position = 1300, separatorBefore = 1250),
    @ActionReference(path = "Shortcuts", name = "DO-C")
})
@Messages("CTL_CopyComponentAsImageAction=Copy as image")
public final class CopyComponentAsImageAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        var tc = WindowManager.getDefault().getRegistry().getActivated();
        if (tc != null) {
            var image = new BufferedImage(tc.getWidth(), tc.getHeight(), BufferedImage.TYPE_INT_ARGB);
            var g2d = image.createGraphics();
            tc.paint(g2d);
            g2d.dispose();

            var transferableImage = new TransferableImage(image);
            var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(transferableImage, null);
        }
    }

    class TransferableImage implements Transferable {

        private final Image mImage;

        public TransferableImage(Image image) {
            mImage = image;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }

            return mImage;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

    }
}

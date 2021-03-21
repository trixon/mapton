/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.core.ui;

import java.io.File;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.mapton.base.ui.file_drop_switchboard.FileDropSwitchboardView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class FileDropSwitchboard {

    private final JButton mDefaultButton = new JButton(Dict.OPEN.toString());
    private final List<File> mFiles;

    public FileDropSwitchboard(List<File> files) {
        mFiles = files;
        displayDialog();
    }

    private void displayDialog() {
        JButton[] buttons = new JButton[]{new JButton(Dict.CANCEL.toString()), mDefaultButton};
        SwitchboardDialogPanel dialogPanel = new SwitchboardDialogPanel(mFiles);
        dialogPanel.initFx(() -> {
        });
        dialogPanel.setPreferredSize(SwingHelper.getUIScaledDim(640, 480));

        NotifyDescriptor d = new NotifyDescriptor(
                dialogPanel,
                Dict.FILE_OPENER.toString(),
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE,
                buttons,
                mDefaultButton);

        if (mDefaultButton == DialogDisplayer.getDefault().notify(d)) {
            dialogPanel.openFiles();
        }
    }

    public class SwitchboardDialogPanel extends FxDialogPanel {

        private FileDropSwitchboardView mFileOpenerView;
        private final List<File> mFiles;
        private BorderPane mRoot;

        public SwitchboardDialogPanel(List<File> files) {
            mFiles = files;
        }

        @Override
        protected void fxConstructor() {
            setScene(createScene());
            mRoot.setCenter(mFileOpenerView = new FileDropSwitchboardView(mFiles));
            SwingUtilities.invokeLater(() -> {
                mDefaultButton.setEnabled(mFileOpenerView.hasFiles());
            });
        }

        void openFiles() {
            mFileOpenerView.openFiles();
        }

        private Scene createScene() {
            return new Scene(mRoot = new BorderPane());
        }
    }
}

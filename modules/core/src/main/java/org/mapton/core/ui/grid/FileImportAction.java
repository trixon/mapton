/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.core.ui.grid;

import java.io.IOException;
import javafx.scene.Node;
import javax.swing.JFileChooser;
import org.controlsfx.control.action.Action;
import se.trixon.almond.nbp.FileChooserHelper;
import org.mapton.api.MNotificationIcons;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxActionSwing;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.FileHelper;

/**
 *
 * @author Patrik Karlström
 */
public class FileImportAction extends FileAction {

    public FileImportAction() {
    }

    @Override
    public Action getAction(Node owner) {
        FxActionSwing action = new FxActionSwing(Dict.IMPORT.toString(), () -> {
            var dialogTitle = "%s %s".formatted(Dict.IMPORT.toString(), mTitle.toLowerCase());
            var extensionFilters = FileChooserHelper.getExtensionFilters();
            var fileChooser = new FileChooserBuilder(FileImportAction.class)
                    .addFileFilter(extensionFilters.get("grid"))
                    .setDefaultWorkingDirectory(FileHelper.getDefaultDirectory())
                    .setFileFilter(extensionFilters.get("grid"))
                    .setFilesOnly(true)
                    .setTitle(dialogTitle)
                    .setSelectionApprover(FileChooserHelper.getFileExistOpenSelectionApprover(Almond.getFrame()))
                    .createFileChooser();

            if (fileChooser.showOpenDialog(Almond.getFrame()) == JFileChooser.APPROVE_OPTION) {
                new Thread(() -> {
                    var file = fileChooser.getSelectedFile();
                    try {
                        mManager.gridImport(file);
                        NotificationDisplayer.getDefault().notify(
                                Dict.OPERATION_COMPLETED.toString(),
                                MNotificationIcons.getInformationIcon(),
                                dialogTitle,
                                null,
                                NotificationDisplayer.Priority.LOW
                        );
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }, getClass().getCanonicalName()).start();
            }
        });

        action.setGraphic(MaterialIcon._File.FOLDER_OPEN.getImageView(getIconSizeToolBarInt()));

        return action;
    }
}

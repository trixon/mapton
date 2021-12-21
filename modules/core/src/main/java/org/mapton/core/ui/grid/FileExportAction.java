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
package org.mapton.core.ui.grid;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.scene.Node;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.action.Action;
import org.mapton.api.MLocalGrid;
import org.mapton.api.MNotificationIcons;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxActionSwing;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
public class FileExportAction extends FileAction {

    private File mFile;

    public FileExportAction() {
    }

    @Override
    public Action getAction(Node owner) {
        FxActionSwing action = new FxActionSwing(Dict.EXPORT.toString(), () -> {
            ArrayList<MLocalGrid> selectedGrids = new ArrayList<>();
            mManager.getItems().stream()
                    .filter((grid) -> (grid.isVisible()))
                    .forEachOrdered((grid) -> {
                        selectedGrids.add(grid);
                    });
            if (!selectedGrids.isEmpty()) {
                SimpleDialog.clearFilters();
                SimpleDialog.addFilters("grid");
                SimpleDialog.setFilter("grid");

                final String dialogTitle = String.format("%s %s", Dict.EXPORT.toString(), mTitle.toLowerCase());
                SimpleDialog.setTitle(dialogTitle);

                if (mFile == null) {
                    SimpleDialog.setPath(FileUtils.getUserDirectory());
                } else {
                    SimpleDialog.setPath(mFile.getParentFile());
                    SimpleDialog.setSelectedFile(new File(""));
                }

                if (SimpleDialog.saveFile(new String[]{"grid"})) {
                    new Thread(() -> {
                        mFile = SimpleDialog.getPath();
                        try {
                            mManager.gridExport(mFile, selectedGrids);
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
            }
        });

        action.setGraphic(MaterialIcon._Content.SAVE.getImageView(getIconSizeToolBarInt()));

        return action;
    }
}

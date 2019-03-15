/*
 * Copyright 2019 Patrik Karlström.
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
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.action.Action;
import org.mapton.api.MKey;
import org.mapton.api.MLocalGrid;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.dialogs.SimpleDialog;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class GridFileExportAction extends GridFileAction {

    private File mFile;

    @Override
    public Action getAction() {
        Action action = new Action((t) -> {
            ArrayList<MLocalGrid> selectedGrids = new ArrayList<>();
            mManager.getItems().stream()
                    .filter((grid) -> (grid.isVisible()))
                    .forEachOrdered((grid) -> {
                        selectedGrids.add(grid);
                    });
            if (!selectedGrids.isEmpty()) {
                SimpleDialog.clearFilters();
                SimpleDialog.addFilter(mExtGrid);
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
                            Mapton.notification(MKey.NOTIFICATION_INFORMATION, dialogTitle, Dict.OPERATION_COMPLETED.toString());
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }).start();
                }
            }
        });

        action.setGraphic(MaterialIcon._Content.SAVE.getImageView(getIconSizeToolBarInt()));

        return action;
    }
}

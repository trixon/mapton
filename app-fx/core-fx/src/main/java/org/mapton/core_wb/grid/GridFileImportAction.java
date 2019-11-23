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
package org.mapton.core_wb.grid;

import java.io.File;
import java.io.IOException;
import javafx.scene.Node;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.glyphfont.FontAwesome;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
public class GridFileImportAction extends GridFileAction {

    private File mFile;

    @Override
    public Action getAction(Node owner) {
        Action action = new Action((t) -> {
            SimpleDialog.clearFilters();
            SimpleDialog.addFilter(mExtGrid);
            final String dialogTitle = String.format("%s %s", Dict.IMPORT.toString(), mTitle.toLowerCase());
            SimpleDialog.setTitle(dialogTitle);
            SimpleDialog.setOwner(owner.getScene().getWindow());

            if (mFile == null) {
                SimpleDialog.setPath(FileUtils.getUserDirectory());
            } else {
                SimpleDialog.setPath(mFile.getParentFile());
                SimpleDialog.setSelectedFile(new File(""));
            }

            if (SimpleDialog.openFile()) {
                new Thread(() -> {
                    mFile = SimpleDialog.getPath();
                    try {
                        mManager.gridImport(mFile);
                        Mapton.notification(MKey.NOTIFICATION_INFORMATION, dialogTitle, Dict.OPERATION_COMPLETED.toString());
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }).start();
            }
        });

        action.setGraphic(Mapton.createGlyphToolbarForm(FontAwesome.Glyph.FOLDER_ALT));

        return action;
    }
}

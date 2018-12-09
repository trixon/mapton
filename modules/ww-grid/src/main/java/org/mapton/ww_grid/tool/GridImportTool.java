/*
 * Copyright 2018 Patrik Karlström.
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
package org.mapton.ww_grid.tool;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.action.Action;
import org.mapton.api.MDict;
import org.mapton.api.MTool;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MTool.class)
public class GridImportTool extends GridTool {

    private File mFile;

    @Override
    public Action getAction() {
        String title = Dict.IMPORT.toString();
        Action action = new Action(title, (t) -> {
            SimpleDialog.clearFilters();
            SimpleDialog.addFilter(mExtGrid);
            SimpleDialog.setTitle(String.format("%s %s", title, MDict.GRID.toString().toLowerCase()));

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
                        NbMessage.information(title, Dict.OPERATION_COMPLETED.toString());
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }).start();
            }
        });

        return action;
    }
}

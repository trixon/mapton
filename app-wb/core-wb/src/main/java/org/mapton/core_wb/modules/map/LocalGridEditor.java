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
package org.mapton.core_wb.modules.map;

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.model.WorkbenchDialog;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.mapton.api.MDict;
import org.mapton.api.MLocalGrid;
import org.mapton.api.MLocalGridManager;
import org.mapton.base.ui.grid.LocalGridView;
import org.mapton.core_wb.api.WorkbenchManager;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class LocalGridEditor implements org.mapton.base.ui.grid.LocalGridEditor {

    private final ButtonType mCancelButtonType = new ButtonType(Dict.CANCEL.toString(), ButtonBar.ButtonData.CANCEL_CLOSE);
    private final MLocalGridManager mManager = MLocalGridManager.getInstance();
    private final ButtonType mOkButtonType = new ButtonType(Dict.SAVE.toString(), ButtonBar.ButtonData.OK_DONE);
    private final Workbench mWorkbench = WorkbenchManager.getInstance().getWorkbench();

    public static LocalGridEditor getInstance() {
        return Holder.INSTANCE;
    }

    private LocalGridEditor() {
    }

    @Override
    public void edit(final MLocalGrid aLocalGrid) {
        MLocalGrid newLocalGrid = aLocalGrid;
        boolean add = aLocalGrid == null;
        if (add) {
            newLocalGrid = new MLocalGrid();
        }

        final MLocalGrid localGrid = newLocalGrid;
        LocalGridView editPanel = new LocalGridView();
        editPanel.load(localGrid);
        String title = MDict.GRID.toString();

        WorkbenchDialog dialog = WorkbenchDialog.builder(
                title,
                editPanel,
                mOkButtonType, mCancelButtonType)
                .onResult(buttonType -> {
                    if (buttonType == mOkButtonType) {
                        editPanel.save(localGrid);
                        if (add) {
                            mManager.getItems().add(localGrid);
                        }
                        mManager.sort();
                    }
                }).build();

        mWorkbench.showDialog(dialog);
    }

    @Override
    public void remove(MLocalGrid localGrid) {
        ButtonType okButtonType = new ButtonType(Dict.REMOVE.toString(), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(Dict.CANCEL.toString(), ButtonBar.ButtonData.CANCEL_CLOSE);

        String title = String.format(Dict.Dialog.TITLE_REMOVE_S.toString(), MDict.GRID.toString().toLowerCase()) + "?";
        String message = String.format(Dict.Dialog.MESSAGE_PROFILE_REMOVE.toString(), localGrid.getName());

        WorkbenchDialog dialog = WorkbenchDialog.builder(
                title,
                message,
                okButtonType, cancelButtonType)
                .onResult(buttonType -> {
                    if (buttonType == okButtonType) {
                        mManager.removeAll(localGrid);
                    }
                }).build();

        mWorkbench.showDialog(dialog);
    }

    private static class Holder {

        private static final LocalGridEditor INSTANCE = new LocalGridEditor();
    }
}

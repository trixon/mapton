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
package org.mapton.ww_grid;

import java.util.Arrays;
import java.util.Collection;
import javafx.event.ActionEvent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class LocalView extends BorderPane {

    private final CheckListView<LocalGrid> mListView = new CheckListView<>();
    private final Options mOptions = Options.getInstance();

    public LocalView() {
        createUI();
    }

    private void createUI() {
        Action addAction = new Action(Dict.ADD.toString(), (ActionEvent event) -> {
            edit(null);
        });
        addAction.setGraphic(MaterialIcon._Content.ADD.getImageView(getIconSizeToolBarInt()));

        Action editAction = new Action(Dict.EDIT.toString(), (ActionEvent event) -> {
            edit(getSelected());
        });
        editAction.setGraphic(MaterialIcon._Editor.MODE_EDIT.getImageView(getIconSizeToolBarInt()));

        Action remAction = new Action(Dict.REMOVE.toString(), (ActionEvent event) -> {
            remove(getSelected());
        });
        remAction.setGraphic(MaterialIcon._Content.REMOVE.getImageView(getIconSizeToolBarInt()));

        Action remAllAction = new Action(Dict.REMOVE_ALL.toString(), (ActionEvent event) -> {
            removeAll();
        });
        remAllAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt()));

        Action importAction = new Action(Dict.IMPORT.toString(), (ActionEvent event) -> {
            importGrids();
        });
        importAction.setGraphic(MaterialIcon._File.FILE_DOWNLOAD.getImageView(getIconSizeToolBarInt()));

        Action exportAction = new Action(Dict.EXPORT.toString(), (ActionEvent event) -> {
            exportGrids();
        });
        exportAction.setGraphic(MaterialIcon._File.FILE_UPLOAD.getImageView(getIconSizeToolBarInt()));

        Collection<? extends Action> actions = Arrays.asList(
                addAction,
                editAction,
                remAction,
                remAllAction,
                ActionUtils.ACTION_SEPARATOR,
                importAction,
                exportAction
        );

        ToolBar toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);

        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        toolBar.getItems().stream().filter((item) -> (item instanceof ButtonBase))
                .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
            FxHelper.undecorateButton(buttonBase);
        });

        setTop(toolBar);
        setCenter(mListView);
    }

    private void edit(LocalGrid localGrid) {
    }

    private void exportGrids() {
    }

    private LocalGrid getSelected() {
        return mListView.getSelectionModel().getSelectedItem();
    }

    private void importGrids() {
    }

    private void remove(LocalGrid localGrid) {
    }

    private void removeAll() {
    }
}

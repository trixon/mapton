/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.transformation;

import java.util.Arrays;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MCrsManager;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.LogPanel;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class TransformationView extends BorderPane {

    private final ComboBox<CoordinateReferenceSystem> mDestComboBox = new ComboBox<>();
    private final LogPanel mDestLogPanel = new LogPanel();
    private final MCrsManager mManager = MCrsManager.getInstance();
    private final ComboBox<CoordinateReferenceSystem> mSourceComboBox = new ComboBox<>();
    private final LogPanel mSourceLogPanel = new LogPanel();

    public TransformationView() {
        createUI();
        initListeners();

        mSourceComboBox.setItems(mManager.getSelectedSystems());
        mDestComboBox.setItems(mManager.getSelectedSystems());
    }

    private void clear() {
    }

    private void createUI() {
        var updateAction = new Action(Dict.UPDATE.toString(), event -> {
            update();
        });
        updateAction.setGraphic(MaterialIcon._Action.SYSTEM_UPDATE_ALT.getImageView(getIconSizeToolBarInt()));

        var refreshAction = new Action(Dict.REFRESH.toString(), event -> {
            refreshUpdaters();
        });
        refreshAction.setGraphic(MaterialIcon._Navigation.REFRESH.getImageView(getIconSizeToolBarInt()));

        var clearAction = new Action(Dict.CLEAR.toString(), event -> {
            clear();
        });
        clearAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt()));

        var actions = Arrays.asList(
                updateAction,
                refreshAction,
                clearAction
        );

        var toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.SHOW);
        FxHelper.slimToolBar(toolBar);
        FxHelper.undecorateButtons(toolBar.getItems().stream());

        var gridPane = new GridPane();

        gridPane.addRow(0, mSourceComboBox, mDestComboBox);
        gridPane.addRow(1, mSourceLogPanel, mDestLogPanel);
        FxHelper.autoSizeColumn(gridPane, 2);
        gridPane.prefHeightProperty().bind(heightProperty());
        mSourceLogPanel.prefHeightProperty().bind(gridPane.heightProperty());

        mSourceComboBox.prefWidthProperty().bind(mSourceLogPanel.widthProperty());
        mDestComboBox.prefWidthProperty().bind(mDestLogPanel.widthProperty());

        mSourceComboBox.setCellFactory(list -> new CrsListCell());
        mSourceComboBox.setButtonCell(new CrsListCell());

        mDestComboBox.setCellFactory(list -> new CrsListCell());
        mDestComboBox.setButtonCell(new CrsListCell());

        setTop(toolBar);
        setCenter(gridPane);
    }

    private void initListeners() {
    }

    private void refreshUpdaters() {
    }

    private void update() {
    }

}
